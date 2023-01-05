import { useMutation } from "@tanstack/react-query";
import { FieldErrorsImpl } from "react-hook-form";
import { Form, SubmitHandler, useForm } from "src/app/components/Form";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";

interface TopicConsumerFormProps {
  topicName: string;
  topicTeam: string;
  renderTopicTypeField: () => JSX.Element;
}

const TopicConsumerForm = ({
  topicName,
  topicTeam,
  renderTopicTypeField,
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
      environment: undefined,
      teamname: topicTeam,
      topictype: "Consumer",
      aclIpPrincipleType: "USERNAME",
    },
  });

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
    <>
      <b>CONSUMER FORM</b>
      <Form
        {...topicConsumerForm}
        onSubmit={onSubmitTopicConsumer}
        onError={onErrorTopicConsumer}
      >
        {renderTopicTypeField()}
      </Form>
    </>
  );
};

export default TopicConsumerForm;
