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
import { MouseEvent, useEffect, useState } from "react";
import { Box, Button, Dialog } from "@aivenio/aquarium";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetSchemaRegistryEnvironments } from "src/domain/environment/environment-api.msw";
import { useQuery } from "@tanstack/react-query";
import { schemaRegistryEnvironments } from "src/domain/environment/environment-queries";
import { Environment } from "src/domain/environment";

const mockedData = [
  createMockEnvironmentDTO({ name: "DEV", id: "1" }),
  createMockEnvironmentDTO({ name: "TST", id: "2" }),
  createMockEnvironmentDTO({ name: "INFRA", id: "3" }),
];

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
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    if (window.msw !== undefined) {
      mockGetSchemaRegistryEnvironments({
        mswInstance: window.msw,
        response: { data: mockedData },
      });
    }
  }, []);

  const { topicName } = props;

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >(schemaRegistryEnvironments());

  const form = useForm<Schema>({
    schema: formSchema,
    defaultValues: {
      topicName: topicName,
    },
  });

  function onSubmitForm(userInput: Schema) {
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
      <Dialog
        title={"this is a dialog"}
        type={"confirmation"}
        open={modalOpen}
        primaryAction={{
          text: "do something",
          onClick: () => {
            console.log("primary");
          },
        }}
      >
        this is a dialog 👋
      </Dialog>
      <Button onClick={() => setModalOpen(!modalOpen)}>
        Click to toggle modal
      </Button>
      <Form
        {...form}
        ariaLabel={"Request a new schema"}
        onSubmit={onSubmitForm}
        onError={onErrorForm}
      >
        {environmentsIsLoading && (
          <div data-testid={"environments-select-loading"}>
            <NativeSelect.Skeleton />
          </div>
        )}
        {environments && (
          <NativeSelect
            name={"environment"}
            labelText={"Select environment"}
            defaultValue={"."}
            required={true}
          >
            <option disabled value={"."}>
              please select
            </option>
            {environments.map((env, index) => {
              return (
                <option key={`${env.name}${index}`} value={env.id}>
                  {env.name}
                </option>
              );
            })}
          </NativeSelect>
        )}

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
