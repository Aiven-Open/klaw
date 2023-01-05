import { useMutation } from "@tanstack/react-query";
import { FieldErrorsImpl } from "react-hook-form";
import { Form, SubmitHandler, useForm } from "src/app/components/Form";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";

interface TopicProducerFormProps {
  topicName: string;
  topicTeam: string;
  renderTopicTypeField: () => JSX.Element;
}

const TopicProducerForm = ({
  topicName,
  topicTeam,
  renderTopicTypeField,
}: TopicProducerFormProps) => {
  const topicProducerForm = useForm<TopicProducerFormSchema>({
    schema: topicProducerFormSchema,
    defaultValues: {
      remarks: undefined,
      consumergroup: undefined,
      acl_ip: undefined,
      acl_ssl: undefined,
      aclPatternType: "LITERAL",
      topicname: topicName,
      environment: undefined,
      teamname: topicTeam,
      topictype: "Producer",
      aclIpPrincipleType: "USERNAME",
    },
  });

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
    <>
      <b>PRODUCER FORM</b>

      <Form
        {...topicProducerForm}
        onSubmit={onSubmitTopicProducer}
        onError={onErrorTopicProducer}
      >
        {renderTopicTypeField()}
      </Form>
    </>
  );
};

export default TopicProducerForm;
