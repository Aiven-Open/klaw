import {
  Alert,
  Box,
  Divider,
  Grid,
  GridItem,
  RadioButton as BaseRadioButton,
  SecondaryButton,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { UseFormReturn } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import {
  Form,
  RadioButtonGroup,
  SubmitButton,
  SubmitHandler,
  TextInput,
} from "src/app/components/Form";
import AclIpPrincipleTypeField from "src/app/features/topics/acl-request/fields/AclIpPrincipleTypeField";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import RemarksField from "src/app/features/topics/acl-request/fields/RemarksField";
import TopicNameOrPrefixField from "src/app/features/topics/acl-request/fields/TopicNameOrPrefixField";
import { TopicProducerFormSchema } from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
import { createAclRequest } from "src/domain/acl/acl-api";
import { ClusterInfo, Environment } from "src/domain/environment";
import { parseErrorMsg } from "src/services/mutation-utils";

// eslint-disable-next-line import/exports-last
export interface TopicProducerFormProps {
  topicProducerForm: UseFormReturn<TopicProducerFormSchema>;
  topicNames: string[];
  environments: Environment[];
  renderAclTypeField: () => JSX.Element;
  clusterInfo?: ClusterInfo;
}

const TopicProducerForm = ({
  topicProducerForm,
  topicNames,
  environments,
  renderAclTypeField,
  clusterInfo,
}: TopicProducerFormProps) => {
  const navigate = useNavigate();
  const { aclIpPrincipleType, aclPatternType } = topicProducerForm.getValues();
  const { current: initialAclIpPrincipleType } = useRef(aclIpPrincipleType);
  const { current: initialAclPatternType } = useRef(aclPatternType);

  // Reset values of acl_ip and acl_ssl when user switches between IP or Principal
  // Not doing so results in values persisting to the form values if a value is entered and the field is then switched
  // Which causes errors
  useEffect(() => {
    // Prevents resetting when switching from Producer to Consumer forms
    if (aclIpPrincipleType === initialAclIpPrincipleType) {
      return;
    }

    topicProducerForm.resetField("acl_ip");
    topicProducerForm.resetField("acl_ssl");
  }, [aclIpPrincipleType]);

  // Reset values of topicname when user switches between LITERAL and PREFIXED
  // Avoids conflict when entering a prefix that is not an existing topic name
  useEffect(() => {
    // Prevents resetting when switching from Producer to Consumer forms
    if (aclPatternType === initialAclPatternType) {
      return;
    }
    topicProducerForm.resetField("topicname");
  }, [aclPatternType]);

  const { mutate, isLoading, isError, error } = useMutation({
    mutationFn: createAclRequest,
  });

  const onSubmitTopicProducer: SubmitHandler<TopicProducerFormSchema> = (
    formData
  ) => {
    mutate(formData);
    navigate(-1);
  };

  return (
    <>
      {isError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert description={parseErrorMsg(error)} type="warning" />
        </Box>
      )}
      <Form {...topicProducerForm} onSubmit={onSubmitTopicProducer}>
        <Grid cols="2" minWidth={"fit"} colGap={"9"}>
          <GridItem>{renderAclTypeField()}</GridItem>
          <GridItem>
            <EnvironmentField environments={environments} />
          </GridItem>

          <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
            <Divider />
          </GridItem>

          <GridItem>
            <RadioButtonGroup
              name="aclPatternType"
              labelText="Topic pattern type"
              required
            >
              <BaseRadioButton value="LITERAL">Literal</BaseRadioButton>
              <BaseRadioButton value="PREFIXED">Prefixed</BaseRadioButton>
            </RadioButtonGroup>
          </GridItem>
          <GridItem>
            {aclPatternType === undefined ? (
              <Box data-testid="empty" style={{ height: "87px" }} />
            ) : (
              <TopicNameOrPrefixField
                topicNames={topicNames}
                aclPatternType={aclPatternType}
              />
            )}
          </GridItem>

          <GridItem colSpan={"span-2"}>
            <TextInput
              name="transactionalId"
              labelText="Transactional ID"
              placeholder="Necessary for exactly-once semantics on producer"
            />
          </GridItem>

          <GridItem>
            <AclIpPrincipleTypeField clusterInfo={clusterInfo} />
          </GridItem>
          <GridItem>
            {aclIpPrincipleType === undefined || clusterInfo === undefined ? (
              <Box data-testid={"empty"} style={{ height: "87px" }} />
            ) : (
              <IpOrPrincipalField
                aclIpPrincipleType={aclIpPrincipleType}
                clusterInfo={clusterInfo}
              />
            )}
          </GridItem>

          <GridItem colSpan={"span-2"} minWidth={"full"}>
            <RemarksField />
          </GridItem>
        </Grid>

        <Grid cols={"2"} colGap={"4"} width={"fit"}>
          <GridItem>
            <SubmitButton loading={isLoading}>Submit</SubmitButton>
          </GridItem>
          <GridItem>
            <SecondaryButton disabled={isLoading}>Cancel</SecondaryButton>
          </GridItem>
        </Grid>
      </Form>
    </>
  );
};

export default TopicProducerForm;
