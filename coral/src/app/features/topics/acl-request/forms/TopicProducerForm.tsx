import {
  Divider,
  Grid,
  GridItem,
  RadioButton as BaseRadioButton,
  SecondaryButton,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect } from "react";
import { FieldErrorsImpl } from "react-hook-form";
import {
  Form,
  RadioButtonGroup,
  SubmitButton,
  SubmitHandler,
  TextInput,
  useForm,
} from "src/app/components/Form";
import AclIpPrincipleTypeField from "src/app/features/topics/acl-request/fields/AclIpPrincipleTypeField";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import RemarksField from "src/app/features/topics/acl-request/fields/RemarksField";
import TopicNameOrPrefixField from "src/app/features/topics/acl-request/fields/TopicNameOrPrefixField";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
import { Environment } from "src/domain/environment";

interface TopicProducerFormProps {
  topicName: string;
  topicNames: string[];
  topicTeam: string;
  environments: Environment[];
  isAivenCluster: boolean;
  renderAclTypeField: () => JSX.Element;
}

const TopicProducerForm = ({
  topicName,
  topicNames,
  topicTeam,
  environments,
  renderAclTypeField,
  isAivenCluster,
}: TopicProducerFormProps) => {
  const topicProducerForm = useForm<TopicProducerFormSchema>({
    schema: topicProducerFormSchema,
    defaultValues: {
      remarks: undefined,
      acl_ip: undefined,
      acl_ssl: undefined,
      aclPatternType: undefined,
      topicname: topicName,
      environment: "placeholder",
      topictype: "Producer",
      aclIpPrincipleType: isAivenCluster ? "PRINCIPAL" : undefined,
      transactionalId: undefined,
    },
  });

  const { aclIpPrincipleType, aclPatternType } = topicProducerForm.getValues();

  // Reset values of acl_ip and acl_ssl when user switches between IP or Principal
  // Not doing so results in values from one field to be persisted to the other after switching
  // Which causes errors
  useEffect(() => {
    topicProducerForm.resetField("acl_ip");
    topicProducerForm.resetField("acl_ssl");
  }, [aclIpPrincipleType]);

  // Reset values of topicname when user switches between LITERAL and PREFIXED
  // Avoids conflict when entering a prefix that is not an existing topic name
  useEffect(() => {
    topicProducerForm.resetField("topicname");
  }, [aclPatternType]);

  const { mutate } = useMutation(() => Promise.resolve());
  const onSubmitTopicProducer: SubmitHandler<TopicProducerFormSchema> = (
    data
  ) => {
    console.log(data, topicTeam);
    mutate();
  };
  const onErrorTopicProducer = (
    err: Partial<FieldErrorsImpl<TopicProducerFormSchema>>
  ) => {
    console.log("Form error", err);
  };
  return (
    <Form
      {...topicProducerForm}
      onSubmit={onSubmitTopicProducer}
      onError={onErrorTopicProducer}
    >
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
          <TopicNameOrPrefixField
            topicNames={topicNames}
            aclPatternType={aclPatternType}
          />
        </GridItem>

        <GridItem colSpan={"span-2"}>
          <TextInput
            name="transactionalId"
            labelText="Transactional ID"
            placeholder="Necessary for exactly-once semantics on producer"
            helperText="Necessary for exactly-once semantics on producer"
          />
        </GridItem>

        <GridItem>
          <AclIpPrincipleTypeField isAivenCluster={isAivenCluster} />
        </GridItem>
        <GridItem>
          <IpOrPrincipalField aclIpPrincipleType={aclIpPrincipleType} />
        </GridItem>
        <GridItem colSpan={"span-2"} minWidth={"full"}>
          <RemarksField />
        </GridItem>
      </Grid>

      <Grid cols={"2"} colGap={"4"} width={"fit"}>
        <GridItem>
          <SubmitButton>Submit</SubmitButton>
        </GridItem>
        <GridItem>
          <SecondaryButton>Cancel</SecondaryButton>
        </GridItem>
      </Grid>
    </Form>
  );
};

export default TopicProducerForm;
