import {
  RadioButton as BaseRadioButton,
  Box,
  Button,
  Divider,
  Grid,
  Option,
  useToast,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
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
import { Routes } from "src/app/router_utils";
import { addNewCluster } from "src/domain/cluster";
import { parseErrorMsg } from "src/services/mutation-utils";

const AddNewClusterForm = () => {
  const navigate = useNavigate();
  const toast = useToast();
  const form = useForm<AddNewClusterFormSchema>({
    schema: addNewClusterFormSchema,
    defaultValues: {
      clusterType: "KAFKA",
      protocol: "PLAINTEXT",
      kafkaFlavor: "APACHE_KAFKA",
    },
  });
  const { clusterType, kafkaFlavor, clusterName } = form.watch();

  const addNewClusterMutation = useMutation(addNewCluster, {
    onSuccess: () => {
      navigate(`${Routes.CLUSTERS}?search=${clusterName}`);
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

  const onSubmitForm = (userInput: AddNewClusterFormSchema) => {
    const formattedBootstrapServers = userInput.bootstrapServers.join(",");
    const formattedAssociatedServers = userInput.associatedServers?.join(",");

    addNewClusterMutation.mutate({
      ...userInput,
      bootstrapServers: formattedBootstrapServers,
      associatedServers: formattedAssociatedServers,
    });
  };

  return (
    <Form {...form} ariaLabel={"Add new cluster"} onSubmit={onSubmitForm}>
      <RadioButtonGroup<AddNewClusterFormSchema>
        name="clusterType"
        labelText="Cluster type"
        required
      >
        <BaseRadioButton name="KAFKA" value="KAFKA">
          Kafka
        </BaseRadioButton>
        <BaseRadioButton name="KAFKA_CONNECT" value="KAFKA_CONNECT">
          Kafka Connect
        </BaseRadioButton>
        <BaseRadioButton name="SCHEMA_REGISTRY" value="SCHEMA_REGISTRY">
          Schema registry
        </BaseRadioButton>
      </RadioButtonGroup>
      <Box marginBottom={"l2"}>
        <Divider />
      </Box>
      <TextInput<AddNewClusterFormSchema>
        name="clusterName"
        labelText="Cluster name"
        placeholder="DEV"
        required
      />

      <NativeSelect<AddNewClusterFormSchema>
        name="protocol"
        labelText="Protocol"
        required
      >
        <Option value="PLAINTEXT">PLAINTEXT</Option>
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
      </NativeSelect>
      <Box marginBottom={"l2"} marginTop={"2"}>
        <Divider />
      </Box>
      <NativeSelect<AddNewClusterFormSchema>
        name="kafkaFlavor"
        labelText="Kafka flavor"
        required
      >
        <Option value="APACHE_KAFKA">Apache Kafka</Option>
        <Option value="AIVEN_FOR_APACHE_KAFKA">Aiven for Apache Kafka</Option>
        <Option value="CONFLUENT">Confluent</Option>
        <Option value="CONFLUENT_CLOUD">Confluent Cloud</Option>
        <Option value="OTHERS">Others</Option>
      </NativeSelect>

      {kafkaFlavor === "AIVEN_FOR_APACHE_KAFKA" && clusterType === "KAFKA" && (
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
      <Box marginBottom={"l2"} marginTop={"2"}>
        <Divider />
      </Box>
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
  );
};
export default AddNewClusterForm;
