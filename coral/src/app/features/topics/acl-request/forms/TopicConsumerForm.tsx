import {
  Box,
  Divider,
  Grid,
  GridItem,
  Option,
  RadioButton as BaseRadioButton,
  SecondaryButton,
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
  TextInput,
  useForm,
} from "src/app/components/Form";
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
  renderACLTypeField: () => JSX.Element;
}

const TopicConsumerForm = ({
  topicName,
  topicNames,
  topicTeam,
  environments,
  renderACLTypeField,
  isAivenCluster,
}: TopicConsumerFormProps) => {
  const topicConsumerForm = useForm<TopicConsumerFormSchema>({
    schema: topicConsumerFormSchema,
    defaultValues: {
      remarks: undefined,
      consumergroup: undefined,
      acl_ip: undefined,
      acl_ssl: undefined,
      aclPatternType: "LITERAL",
      topicname: topicName,
      environment: "placeholder",
      teamname: topicTeam,
      topictype: "Consumer",
      aclIpPrincipleType: isAivenCluster ? "PRINCIPAL" : undefined,
    },
  });

  const renderAclIpPrincipleTypeInput = () => {
    const type = topicConsumerForm.getValues("aclIpPrincipleType");

    if (type === undefined) {
      return <Box style={{ height: "87px" }} />;
    }

    return type === "IP_ADDRESS" ? (
      <MultiInput
        name="acl_ip"
        labelText="IP addresses"
        placeholder="192.168.1.1, 2606:4700:4700::1111"
        required
      />
    ) : (
      <MultiInput
        name="acl_ssl"
        labelText="SSL DN strings / Usernames"
        placeholder="CN=myhost, Alice"
        required
      />
    );
  };

  const { mutate } = useMutation(() => Promise.resolve());
  const onSubmitTopicConsumer: SubmitHandler<TopicConsumerFormSchema> = (
    data
  ) => {
    console.log(data);
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
          <GridItem>{renderACLTypeField()}</GridItem>
        </GridItem>
        <GridItem>
          <NativeSelect
            name="environment"
            labelText="Select environment"
            required
          >
            <Option key={"Placeholder"} value="placeholder" disabled>
              -- Select Environment --
            </Option>
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

        <GridItem>
          <NativeSelect name="topicName" labelText="Topic name" required>
            <Option key={"Placeholder"} disabled>
              -- Select Topic --
            </Option>
            {topicNames.map((name) => (
              <Option key={name} value={name}>
                {name}
              </Option>
            ))}
          </NativeSelect>
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
          <RadioButtonGroup
            name="aclIpPrincipleType"
            labelText="IP or Principal based"
            required
          >
            <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
              IP
            </BaseRadioButton>
            <BaseRadioButton value="PRINCIPAL">Principal</BaseRadioButton>
          </RadioButtonGroup>
        </GridItem>
        <GridItem>{renderAclIpPrincipleTypeInput()} </GridItem>

        <GridItem colSpan={"span-2"} minWidth={"full"} paddingBottom={"l2"}>
          <Textarea
            name="remarks"
            labelText="Remarks"
            placeholder="Comments about this request."
          />
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
