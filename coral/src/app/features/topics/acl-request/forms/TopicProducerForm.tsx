import {
  Grid,
  GridItem,
  Divider,
  SecondaryButton,
  Option,
  RadioButton as BaseRadioButton,
  Box,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { FieldErrorsImpl } from "react-hook-form";
import {
  Form,
  MultiInput,
  NativeSelect,
  RadioButtonGroup,
  SubmitButton,
  SubmitHandler,
  Textarea,
  useForm,
} from "src/app/components/Form";
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
  renderTopicTypeField: () => JSX.Element;
}

const TopicProducerForm = ({
  topicName,
  topicNames,
  topicTeam,
  environments,
  renderTopicTypeField,
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
      environment: undefined,
      teamname: topicTeam,
      topictype: "Producer",
      aclIpPrincipleType: isAivenCluster ? "PRINCIPAL" : undefined,
    },
  });

  const renderAclIpPrincipleTypeInput = () => {
    const type = topicProducerForm.getValues("aclIpPrincipleType");

    if (type === undefined) {
      return <Box style={{ height: "87px" }} />;
    }

    return type === "IP_ADDRESS" ? (
      <MultiInput name="acl_ip" labelText="IPs" required />
    ) : (
      <MultiInput name="acl_ssl" labelText="Usernames" required />
    );
  };

  const renderPrefixInput = () => {
    const isPrefixed = topicProducerForm.getValues("aclPatternType");

    if (isPrefixed === "LITERAL") {
      return <Box style={{ height: "87px" }} />;
    }

    return <MultiInput name="acl_ip" labelText="Prefix" required />;
  };

  const { mutate } = useMutation(() => Promise.resolve());
  const onSubmitTopicProducer: SubmitHandler<TopicProducerFormSchema> = (
    data
  ) => {
    console.log(data);
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
        <GridItem>
          <NativeSelect name="topicName" labelText="Topic name" required>
            <Option key={"Placeholder"}>-- Select Topic --</Option>
            {topicNames.map((name) => (
              <Option
                key={name}
                value={name}
                selected={topicName === name}
                disabled={topicName !== name}
              >
                {name}
              </Option>
            ))}
          </NativeSelect>
        </GridItem>
        <GridItem>
          <NativeSelect
            name="environment"
            labelText="Select environment"
            required
          >
            <Option key={"Placeholder"}>-- Select Environment --</Option>
            {environments.map((env) => (
              <Option key={env.id} value={env.id}>
                {env.name}
              </Option>
            ))}
          </NativeSelect>
        </GridItem>

        <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
          <Divider />
        </GridItem>

        <GridItem>{renderTopicTypeField()}</GridItem>
        <GridItem>
          <Box style={{ height: "87px" }} />
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
          <GridItem>{renderPrefixInput()}</GridItem>
        </GridItem>
        <GridItem>
          <RadioButtonGroup
            name="aclIpPrincipleType"
            labelText="IP or Username based"
            required
          >
            <BaseRadioButton value="PRINCIPAL">Username</BaseRadioButton>
            <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
              IP
            </BaseRadioButton>
          </RadioButtonGroup>
        </GridItem>

        <GridItem>{renderAclIpPrincipleTypeInput()} </GridItem>
        <GridItem colSpan={"span-2"} minWidth={"full"}>
          <Textarea name="remarks" labelText="Remarks" />
        </GridItem>
      </Grid>
      <Grid cols={"2"} colGap={"4"} width={"fit"}>
        <GridItem>
          <SubmitButton>Submit</SubmitButton>
        </GridItem>
        <GridItem>
          <SecondaryButton>Cancel</SecondaryButton>
        </GridItem>
      </Grid>{" "}
    </Form>
  );
};

export default TopicProducerForm;
