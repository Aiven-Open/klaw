import {
  Alert,
  Box,
  Divider,
  Grid,
  GridItem,
  SecondaryButton,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { UseFormReturn } from "react-hook-form";
import { useNavigate } from "react-router-dom";
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
import { TopicConsumerFormSchema } from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import { createAclRequest } from "src/domain/acl/acl-api";
import { Environment } from "src/domain/environment";
import { parseErrorMsg } from "src/services/mutation-utils";

// eslint-disable-next-line import/exports-last
export interface TopicConsumerFormProps {
  topicConsumerForm: UseFormReturn<TopicConsumerFormSchema>;
  topicNames: string[];
  environments: Environment[];
  renderAclTypeField: () => JSX.Element;
  isAivenCluster?: boolean;
}

const TopicConsumerForm = ({
  topicConsumerForm,
  topicNames,
  environments,
  renderAclTypeField,
  isAivenCluster,
}: TopicConsumerFormProps) => {
  const navigate = useNavigate();
  const { aclIpPrincipleType } = topicConsumerForm.getValues();
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
    onSuccess: () =>
      window.location.assign("/myAclRequests?reqsType=created&aclCreated=true"),
  });

  const onSubmitTopicConsumer: SubmitHandler<TopicConsumerFormSchema> = (
    formData
  ) => {
    mutate(formData);
  };

  const hideConsumerGroupField = isAivenCluster === undefined || isAivenCluster;
  const hideIpOrPrincipalField =
    aclIpPrincipleType === undefined || isAivenCluster === undefined;

  useEffect(() => {
    if (hideConsumerGroupField) {
      topicConsumerForm.setValue("consumergroup", "-na-");
    } else {
      topicConsumerForm.setValue("consumergroup", "");
    }
  }, [hideConsumerGroupField]);

  return (
    <>
      {isError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert type="warning">{parseErrorMsg(error)}</Alert>
        </Box>
      )}
      <Form {...topicConsumerForm} onSubmit={onSubmitTopicConsumer}>
        <Grid cols="2" minWidth={"fit"} colGap={"9"}>
          <GridItem>{renderAclTypeField()}</GridItem>
          <GridItem>
            <EnvironmentField environments={environments} />
          </GridItem>

          <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
            <Divider />
          </GridItem>

          <GridItem>
            <TopicNameField topicNames={topicNames} />
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
              />
            )}
          </GridItem>

          <GridItem colSpan={"span-2"} minWidth={"full"} paddingBottom={"l2"}>
            <RemarksField />
          </GridItem>
        </Grid>

        <Grid cols={"2"} colGap={"4"} width={"fit"}>
          <GridItem>
            <SubmitButton loading={isLoading}>Submit</SubmitButton>
          </GridItem>
          <GridItem>
            <SecondaryButton disabled={isLoading} onClick={() => navigate(-1)}>
              Cancel
            </SecondaryButton>
          </GridItem>
        </Grid>
      </Form>
    </>
  );
};

export default TopicConsumerForm;
