import z from "zod";
import {
  useForm,
  Form,
  NativeSelect,
  SubmitButton,
  FileInput,
  Textarea,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";
import { MouseEvent } from "react";
import { Box, Button } from "@aivenio/aquarium";
import { Simulate } from "react-dom/test-utils";
import submit = Simulate.submit;

type TopicSchemaRequestProps = {
  topicName: string;
};

const formSchema = z.object({
  environment: z.string().min(1, { message: "env is required" }),
  topicName: z.string(),
  schemafull: z.any().optional(),
  remarks: z.string().optional(),
});

type Schema = z.infer<typeof formSchema>;

function TopicSchemaRequest(props: TopicSchemaRequestProps) {
  const { topicName } = props;

  const form = useForm<Schema>({
    schema: formSchema,
    defaultValues: {
      topicName: topicName,
    },
  });

  function onSubmitForm(userInput: Schema) {
    console.log(submit);
    console.log("onSubmit userInput", userInput);
  }

  function onErrorForm(arg: FieldErrors) {
    console.log("onError", arg);
  }

  function onCancel(event: MouseEvent<HTMLButtonElement>) {
    console.log("onCancel", event);
  }

  return (
    <>
      <Form
        {...form}
        ariaLabel={"Request a new schema"}
        onSubmit={onSubmitForm}
        onError={onErrorForm}
      >
        <NativeSelect
          name={"environment"}
          labelText={"Select environment"}
          defaultValue={"."}
          required={true}
        >
          <option disabled value={"."}>
            please select
          </option>
          <option value={"1"}>First env</option>
          <option value={"2"}>Second env</option>
          <option value={"3"}>Third env</option>
        </NativeSelect>

        <NativeSelect
          name={"topicName"}
          labelText={"Topic name"}
          defaultValue={topicName}
          readOnly={true}
          aria-readonly={true}
        >
          <option value={topicName}>{topicName}</option>
        </NativeSelect>

        <FileInput
          buttonText={"Upload AVRO Schema"}
          labelText={"Upload AVRO Schema File"}
          name={"schemafull"}
          noFileText={"No file chosen"}
          required={true}
        />

        <Textarea name={"remarks"} labelText={"Message for the approval"} />
        <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
          <SubmitButton>Submit request</SubmitButton>
          <Button type="button" kind={"secondary"} onClick={onCancel}>
            Cancel
          </Button>
        </Box>
      </Form>
    </>
  );
}

export { TopicSchemaRequest };
