import {
  RadioButton as BaseRadioButton,
  Button,
  Grid,
  Option,
  useToast,
  Alert,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Form,
  MultiInput,
  NativeSelect,
  RadioButtonGroup,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import {
  AddNewClusterFormSchema,
  addNewClusterFormSchema,
} from "src/app/features/configuration/clusters/schemas/add-new-cluster-form";
import { addNewCluster } from "src/domain/cluster";
import { parseErrorMsg } from "src/services/mutation-utils";
import { Routes } from "src/services/router-utils/types";
import { kafkaFlavorToString } from "src/services/formatter/kafka-flavor-formatter";
import { clusterTypeToString } from "src/services/formatter/cluster-type-formatter";
import { Dialog } from "src/app/components/Dialog";

const AddNewClusterForm = () => {
  const [confirmPlaintextProctocolDialog, setConfirmPlaintextProctocolDialog] =
    useState(false);

  const navigate = useNavigate();
  const toast = useToast();
  const form = useForm<AddNewClusterFormSchema>({
    schema: addNewClusterFormSchema,
    defaultValues: {
      clusterType: "KAFKA",
      protocol: "SSL",
    },
  });
  const { clusterType, kafkaFlavor, clusterName, protocol } = form.watch();

  // Because the protocol fields for the 'kafka' clusterType has more options than the other clusterTypes
  // We need to reset the protocol field when the clusterType changes
  useEffect(() => {
    form.resetField("protocol");
    return;
  }, [clusterType]);

  // Similarly, we need to reset the projectName and serviceName fields when the clusterType or kafkaFlavor changes
  // As those fields should be undefined when the clusterType is not 'KAFKA' and the kafkaFlavor is not 'AIVEN_FOR_APACHE_KAFKA'
  useEffect(() => {
    form.resetField("projectName");
    form.resetField("serviceName");
    return;
  }, [clusterType, kafkaFlavor]);

  const addNewClusterMutation = useMutation(addNewCluster, {
    onSuccess: () => {
      navigate(`${Routes.CLUSTERS}?search=${clusterName}&showConnectHelp=true`);
      toast({
        message: "Cluster successfully added",
        position: "bottom-left",
        variant: "default",
      });
    },
    onError: (error) => {
      toast({
        message: `Could not add cluster: ${parseErrorMsg(error)}`,
        position: "bottom-left",
        variant: "danger",
      });
    },
  });

  const onSubmitForm = () => {
    const userInput: AddNewClusterFormSchema = form.getValues();
    const formattedBootstrapServers = userInput.bootstrapServers.join(",");
    const formattedAssociatedServers = userInput.associatedServers?.join(",");

    addNewClusterMutation.mutate({
      ...userInput,
      bootstrapServers: formattedBootstrapServers,
      associatedServers: formattedAssociatedServers,
    });
  };

  return (
    <>
      {confirmPlaintextProctocolDialog && (
        <Dialog
          title={"PLAINTEXT is not secure"}
          primaryAction={{
            text: "Add new Cluster",
            onClick: () => {
              onSubmitForm();
              setConfirmPlaintextProctocolDialog(false);
            },
          }}
          secondaryAction={{
            text: "Cancel",
            onClick: () => setConfirmPlaintextProctocolDialog(false),
          }}
          type={"warning"}
        >
          Are you sure you want to continue with PLAINTEXT?
        </Dialog>
      )}
      <Form
        {...form}
        ariaLabel={"Add new cluster"}
        onSubmit={
          protocol === "PLAINTEXT"
            ? () => setConfirmPlaintextProctocolDialog(true)
            : onSubmitForm
        }
      >
        <RadioButtonGroup<AddNewClusterFormSchema>
          name="clusterType"
          labelText="Cluster type"
          required
        >
          <BaseRadioButton name="KAFKA" value="KAFKA">
            {clusterTypeToString["KAFKA"]}
          </BaseRadioButton>
          <BaseRadioButton name="KAFKA_CONNECT" value="KAFKA_CONNECT">
            {clusterTypeToString["KAFKA_CONNECT"]}
          </BaseRadioButton>
          <BaseRadioButton name="SCHEMA_REGISTRY" value="SCHEMA_REGISTRY">
            {clusterTypeToString["SCHEMA_REGISTRY"]}
          </BaseRadioButton>
        </RadioButtonGroup>
        <TextInput<AddNewClusterFormSchema>
          name="clusterName"
          labelText="Cluster name"
          placeholder="DEV"
          required
        />

        {protocol === "PLAINTEXT" && (
          <Alert type={"warning"}>
            PLAINTEXT is not secure. Use a secure option like SSL.{" "}
            <a
              href="https://www.klaw-project.io/docs/cluster-management/clusters-environments/"
              aria-label="Documentation Clusters and Environments"
            >
              Learn more
            </a>
          </Alert>
        )}
        <NativeSelect<AddNewClusterFormSchema>
          name="protocol"
          labelText="Protocol"
          required
        >
          <Option value="SSL">SSL</Option>
          {clusterType === "KAFKA" && (
            <>
              <Option value="SASL_PLAIN">SASL_PLAIN</Option>
              <Option value="SASL_SSL_PLAIN_MECHANISM">
                SASL_SSL_PLAIN_MECHANISM
              </Option>
              <Option value="SASL_SSL_GSSAPI_MECHANISM">
                SASL_SSL_GSSAPI_MECHANISM
              </Option>
              <Option value="SASL_SSL_SCRAM_MECHANISM_256">
                SASL_SSL_SCRAM_MECHANISM_256
              </Option>
              <Option value="SASL_SSL_SCRAM_MECHANISM_512">
                SASL_SSL_SCRAM_MECHANISM_512
              </Option>
            </>
          )}
          <Option value="PLAINTEXT">PLAINTEXT (not recommended)</Option>
        </NativeSelect>
        <NativeSelect<AddNewClusterFormSchema>
          name="kafkaFlavor"
          labelText="Kafka flavor"
          placeholder="-- Please select --"
          required
        >
          <Option value="APACHE_KAFKA">
            {kafkaFlavorToString["APACHE_KAFKA"]}
          </Option>
          <Option value="AIVEN_FOR_APACHE_KAFKA">
            {kafkaFlavorToString["AIVEN_FOR_APACHE_KAFKA"]}
          </Option>
          <Option value="CONFLUENT">{kafkaFlavorToString["CONFLUENT"]}</Option>
          <Option value="CONFLUENT_CLOUD">
            {kafkaFlavorToString["CONFLUENT_CLOUD"]}
          </Option>
          <Option value="OTHERS">{kafkaFlavorToString["OTHERS"]}</Option>
        </NativeSelect>

        {kafkaFlavor === "AIVEN_FOR_APACHE_KAFKA" &&
          clusterType === "KAFKA" && (
            <Grid gap={"l3"}>
              <Grid.Item xs={6}>
                <TextInput<AddNewClusterFormSchema>
                  name="projectName"
                  labelText="Project name"
                  placeholder="project-name"
                  width="full"
                  required
                />
              </Grid.Item>
              <Grid.Item xs={6}>
                <TextInput<AddNewClusterFormSchema>
                  name="serviceName"
                  labelText="Service name"
                  placeholder="kafka-service-name"
                  width="full"
                  required
                />
              </Grid.Item>
            </Grid>
          )}
        <MultiInput<AddNewClusterFormSchema>
          name="bootstrapServers"
          labelText="Bootstrap servers"
          placeholder="server:9092"
          required
        />
        <MultiInput<AddNewClusterFormSchema>
          name="associatedServers"
          labelText="REST API servers (optional)"
          placeholder="https://server:8082"
        />

        <Grid cols={"2"} colGap={"5"} width={"fit"} marginTop={"l1"}>
          <Grid.Item>
            <SubmitButton loading={addNewClusterMutation.isLoading}>
              Add new cluster
            </SubmitButton>
          </Grid.Item>
          <Grid.Item>
            <Button.Secondary
              disabled={addNewClusterMutation.isLoading}
              type="button"
              onClick={() => navigate(Routes.CLUSTERS)}
            >
              Cancel
            </Button.Secondary>
          </Grid.Item>
        </Grid>
      </Form>
    </>
  );
};
export default AddNewClusterForm;
