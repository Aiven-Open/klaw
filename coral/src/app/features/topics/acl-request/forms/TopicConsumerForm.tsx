import { Divider, Grid, GridItem, SecondaryButton } from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect } from "react";
import { FieldErrorsImpl } from "react-hook-form";
import {
  Form,
  SubmitButton,
  SubmitHandler,
  TextInput,
  useForm,
} from "src/app/components/Form";
import AclIpPrincipleTypeField from "src/app/features/topics/acl-request/fields/AclIpPrincipleTypeField";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import RemarksField from "src/app/features/topics/acl-request/fields/RemarksField";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import { Environment } from "src/domain/environment";

interface TopicConsumerFormProps {
  topicName: string;
  topicNames: string[];
  topicTeam: string;
  environments: Environment[];
  isAivenCluster: boolean;
  renderAclTypeField: () => JSX.Element;
}

const TopicConsumerForm = ({
  topicName,
  topicNames,
  topicTeam,
  environments,
  renderAclTypeField,
  isAivenCluster,
}: TopicConsumerFormProps) => {
  const topicConsumerForm = useForm<TopicConsumerFormSchema>({
    schema: topicConsumerFormSchema,
    defaultValues: {
      aclPatternType: "LITERAL",
      topicname: topicName,
      environment: "placeholder",
      topictype: "Consumer",
      aclIpPrincipleType: isAivenCluster ? "PRINCIPAL" : undefined,
    },
  });

  const { aclIpPrincipleType } = topicConsumerForm.getValues();

  // Reset values of acl_ip and acl_ssl when user switches between IP or Principal
  // Not doing so results in values from one field to be persisted to the other after switching
  // Which causes errors
  useEffect(() => {
    topicConsumerForm.resetField("acl_ip");
    topicConsumerForm.resetField("acl_ssl");
  }, [aclIpPrincipleType]);

  const { mutate } = useMutation(() => Promise.resolve());
  const onSubmitTopicConsumer: SubmitHandler<TopicConsumerFormSchema> = (
    data
  ) => {
    console.log(data, topicTeam);
    mutate();
  };
  const onErrorTopicConsumer = (
    err: Partial<FieldErrorsImpl<TopicConsumerFormSchema>>
  ) => {
    console.log("Form error", err);
  };

  return (
    <Form
      {...topicConsumerForm}
      onSubmit={onSubmitTopicConsumer}
      onError={onErrorTopicConsumer}
    >
      <Grid cols="2" minWidth={"fit"} colGap={"9"}>
        <GridItem>
          <GridItem>{renderAclTypeField()}</GridItem>
        </GridItem>
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
          <TextInput
            name="consumergroup"
            labelText="Consumer group"
            placeholder="Add consumer group here"
            required
          />
        </GridItem>

        <GridItem>
          <AclIpPrincipleTypeField isAivenCluster={isAivenCluster} />
        </GridItem>
        <GridItem>
          <IpOrPrincipalField aclIpPrincipleType={aclIpPrincipleType} />
        </GridItem>

        <GridItem colSpan={"span-2"} minWidth={"full"} paddingBottom={"l2"}>
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

export default TopicConsumerForm;
