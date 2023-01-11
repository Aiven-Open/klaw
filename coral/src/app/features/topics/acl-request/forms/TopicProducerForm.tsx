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
import { useEffect } from "react";
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
  renderACLTypeField: () => JSX.Element;
}

const TopicProducerForm = ({
  topicName,
  topicNames,
  topicTeam,
  environments,
  renderACLTypeField,
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
      teamname: topicTeam,
      topictype: "Producer",
      aclIpPrincipleType: isAivenCluster ? "PRINCIPAL" : undefined,
      transactionalId: undefined,
    },
  });

  const aclIpPrincipleType = topicProducerForm.getValues("aclIpPrincipleType");
  useEffect(() => {
    topicProducerForm.resetField("acl_ip");
    topicProducerForm.resetField("acl_ssl");
  }, [aclIpPrincipleType]);

  const aclPatternType = topicProducerForm.getValues("aclPatternType");
  useEffect(() => {
    topicProducerForm.resetField("topicname");
    topicProducerForm.resetField("transactionalId");
  }, [aclPatternType]);

  const renderAclIpPrincipleTypeInput = () => {
    const type = topicProducerForm.getValues("aclIpPrincipleType");

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

  const renderAclPatternTypeInput = () => {
    if (aclPatternType === undefined) {
      return <Box style={{ height: "87px" }} />;
    }

    if (aclPatternType === "LITERAL") {
      return (
        <GridItem>
          <NativeSelect name="topicname" labelText="Topic name" required>
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
      );
    }

    if (aclPatternType === "PREFIXED") {
      return <TextInput name="topicname" labelText="Prefix" required />;
    }
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
        <GridItem>{renderACLTypeField()}</GridItem>
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
          <GridItem>{renderAclPatternTypeInput()}</GridItem>
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
          <RadioButtonGroup
            name="aclIpPrincipleType"
            labelText="IP or Username based"
            required
          >
            <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
              IP
            </BaseRadioButton>
            <BaseRadioButton value="PRINCIPAL">Principal</BaseRadioButton>
          </RadioButtonGroup>
        </GridItem>
        <GridItem>{renderAclIpPrincipleTypeInput()} </GridItem>
        <GridItem colSpan={"span-2"} minWidth={"full"}>
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

export default TopicProducerForm;
