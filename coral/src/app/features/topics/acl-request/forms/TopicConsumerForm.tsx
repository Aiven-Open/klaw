import {
  Alert,
  Box,
  Divider,
  Grid,
  GridItem,
  Input,
  Button,
  useToast,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { UseFormReturn } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { Dialog } from "src/app/components/Dialog";
import {
  Form,
  SubmitButton,
  SubmitHandler,
  TextInput,
} from "src/app/components/Form";
import AclIpPrincipleTypeField from "src/app/features/topics/acl-request/fields/AclIpPrincipleTypeField";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import RemarksField from "src/app/features/topics/acl-request/fields/RemarksField";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { TopicConsumerFormSchema } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-consumer";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";
import { createAclRequest } from "src/domain/acl/acl-api";
import { parseErrorMsg } from "src/services/mutation-utils";

// eslint-disable-next-line import/exports-last
export interface TopicConsumerFormProps {
  topicConsumerForm: UseFormReturn<TopicConsumerFormSchema>;
  topicNames: string[];
  environments: ExtendedEnvironment[];
  renderAclTypeField: () => JSX.Element;
  isSubscription: boolean;
  isAivenCluster?: boolean;
}

const TopicConsumerForm = ({
  topicConsumerForm,
  topicNames,
  environments,
  renderAclTypeField,
  isAivenCluster,
  isSubscription,
}: TopicConsumerFormProps) => {
  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const navigate = useNavigate();
  const toast = useToast();

  const { aclIpPrincipleType, environment, topicname } =
    topicConsumerForm.getValues();
  const { current: initialAclIpPrincipleType } = useRef(aclIpPrincipleType);

  // Reset values of acl_ip and acl_ssl when user switches between IP or Principal
  // Not doing so results in values from one field to be persisted to the other after switching
  // Which causes errors
  useEffect(() => {
    // Prevents resetting when switching from Producer to Consumer forms
    if (aclIpPrincipleType === initialAclIpPrincipleType) {
      return;
    }

    topicConsumerForm.resetField("acl_ip");
    topicConsumerForm.resetField("acl_ssl");
  }, [aclIpPrincipleType]);

  const { mutate, isLoading, isError, error } = useMutation({
    mutationFn: createAclRequest,
    onSuccess: () => {
      navigate("/requests/acls?status=CREATED");
      toast({
        message: "ACL request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  const onSubmitTopicConsumer: SubmitHandler<TopicConsumerFormSchema> = (
    formData
  ) => {
    mutate(formData);
  };

  function cancelRequest() {
    topicConsumerForm.reset();
    navigate(-1);
  }

  // The consumer group field is irrelevant if the Environment is an Aiven cluster
  // So we hide it when:
  // - we don't know if the Environment is an Aiven cluster (user has not selected an environment yet)
  // - the selected Environment is an Aiven cluster
  const hideConsumerGroupField = isAivenCluster === undefined || isAivenCluster;
  const hideIpOrPrincipalField =
    aclIpPrincipleType === undefined || isAivenCluster === undefined;

  // Because the consumergroup field is *still* a required field in the form schema,
  // we need to pass it a value even when the field is hidden.
  // This value needs to be "-na-" so that the backend can process it correctly
  useEffect(() => {
    if (hideConsumerGroupField) {
      topicConsumerForm.setValue("consumergroup", "-na-");
    } else {
      // Reset field to e empty value so that the "-na-" value does not persist between switching Environments
      topicConsumerForm.setValue("consumergroup", "");
    }
  }, [hideConsumerGroupField]);

  return (
    <>
      {isError && (
        <Box marginBottom={"l1"}>
          <Alert type="error">{parseErrorMsg(error)}</Alert>
        </Box>
      )}
      <Form
        {...topicConsumerForm}
        ariaLabel={"Request consumer ACL"}
        onSubmit={onSubmitTopicConsumer}
      >
        <Grid cols="2" minWidth={"fit"} colGap={"9"}>
          <GridItem>{renderAclTypeField()}</GridItem>
          <GridItem>
            <EnvironmentField
              environments={environments}
              selectedTopic={topicname}
              readOnly={isSubscription}
            />
          </GridItem>

          <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
            <Divider />
          </GridItem>

          <GridItem>
            {environment === undefined ? (
              <Input
                // This is not really a readOnly field but
                // a placeholder until the user can select value
                // from a list, so I didn't change the label
                labelText="Topic name"
                defaultValue="Select environment first"
                height={45}
                readOnly
                required={true}
              />
            ) : (
              <TopicNameField
                topicNames={topicNames}
                readOnly={isSubscription}
              />
            )}
          </GridItem>
          <GridItem>
            {hideConsumerGroupField ? (
              <Box data-testid={"empty"} style={{ height: "87px" }} />
            ) : (
              <TextInput
                name="consumergroup"
                labelText="Consumer group"
                placeholder="Add Consumer group here"
                required
              />
            )}
          </GridItem>

          <GridItem>
            <AclIpPrincipleTypeField isAivenCluster={isAivenCluster} />
          </GridItem>
          <GridItem>
            {hideIpOrPrincipalField ? (
              <Box data-testid={"empty"} style={{ height: "87px" }} />
            ) : (
              <IpOrPrincipalField
                aclIpPrincipleType={aclIpPrincipleType}
                isAivenCluster={isAivenCluster}
                environment={environment}
              />
            )}
          </GridItem>

          <GridItem colSpan={"span-2"} minWidth={"full"} paddingBottom={"l2"}>
            <RemarksField />
          </GridItem>
        </Grid>

        <Grid cols={"2"} colGap={"4"} width={"fit"}>
          <GridItem>
            <SubmitButton loading={isLoading}>Submit request</SubmitButton>
          </GridItem>
          <GridItem>
            <Button.Secondary
              disabled={isLoading}
              type="button"
              onClick={
                topicConsumerForm.formState.isDirty
                  ? () => setCancelDialogVisible(true)
                  : () => cancelRequest()
              }
            >
              Cancel
            </Button.Secondary>
          </GridItem>
        </Grid>
      </Form>
      {cancelDialogVisible && (
        <Dialog
          title={"Cancel ACL request?"}
          primaryAction={{
            text: "Cancel request",
            onClick: () => cancelRequest(),
          }}
          secondaryAction={{
            text: "Continue with request",
            onClick: () => setCancelDialogVisible(false),
          }}
          type={"warning"}
        >
          Do you want to cancel this request? The data added will be lost.
        </Dialog>
      )}
    </>
  );
};

export default TopicConsumerForm;
