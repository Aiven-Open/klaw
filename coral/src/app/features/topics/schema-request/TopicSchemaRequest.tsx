import {
  useForm,
  Form,
  NativeSelect,
  SubmitButton,
  Textarea,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";
import { MouseEvent, useEffect } from "react";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetSchemaRegistryEnvironments } from "src/domain/environment/environment-api.msw";
import { useQuery } from "@tanstack/react-query";
import {
  Environment,
  getSchemaRegistryEnvironments,
} from "src/domain/environment";
import {
  TopicRequestFormSchema,
  topicRequestFormSchema,
} from "src/app/features/topics/schema-request/utils/zod-schema";
import { TopicSchema } from "src/app/features/topics/schema-request/components/TopicSchema";
import { Box, Button } from "@aivenio/aquarium";

const mockedData = [
  createMockEnvironmentDTO({ name: "DEV", id: "1" }),
  createMockEnvironmentDTO({ name: "TST", id: "2" }),
  createMockEnvironmentDTO({ name: "INFRA", id: "3" }),
];

type TopicSchemaRequestProps = {
  topicName: string;
  // ‼️this property is ONLY for testing purpose!
  schemafullValueForTest?: string;
};

// Note about prop "schemafullValueForTest": It's a bad practice to add/expose code/api
// only for testing purpose. Since "TopicRequest" is using "MonacoEditor" (which is not available
// with full functionality in tests), I couldn't find a way to get the 'schemafull' value to be available
// in this test. I also couldn't find a way to manually set / validate the value in this component.
// without a valid 'schemafull', I couldn't test behavior when submitting the form (submit is not enabled)
// Since that is are important test cases, I opted for the bad practice rather in order to
// be able to add this tests. I created na issue in github related to MonacoEditor testing already.
function TopicSchemaRequest(props: TopicSchemaRequestProps) {
  const { topicName } = props;

  useEffect(() => {
    if (window.msw !== undefined) {
      mockGetSchemaRegistryEnvironments({
        mswInstance: window.msw,
        response: { data: mockedData },
      });
    }
  }, []);

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >({
    queryKey: ["schemaRegistryEnvironments"],
    queryFn: () => getSchemaRegistryEnvironments(),
  });

  const form = useForm<TopicRequestFormSchema>({
    schema: topicRequestFormSchema,
    defaultValues: {
      topicName: topicName,
      schemafull: props.schemafullValueForTest || undefined,
    },
  });

  function onSubmitForm(userInput: TopicRequestFormSchema) {
    console.log("onSubmit userInput", userInput);
  }

  function onErrorForm(arg: FieldErrors) {
    console.log("onError", arg);
  }

  function onCancel(event: MouseEvent<HTMLButtonElement>) {
    console.log("onCancel", event);
  }

  return (
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
        <NativeSelect<TopicRequestFormSchema>
          name={"environment"}
          labelText={"Select environment"}
          defaultValue={""}
          required={true}
        >
          <option disabled value={""}>
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

      <NativeSelect<TopicRequestFormSchema>
        name={"topicName"}
        labelText={"Topic name"}
        defaultValue={topicName}
        readOnly={true}
        aria-readonly={true}
      >
        <option value={topicName}>{topicName}</option>
      </NativeSelect>

      <TopicSchema
        name={"schemafull"}
        required={!props.schemafullValueForTest}
      />

      <Textarea name={"remarks"} labelText={"Message for the approval"} />
      <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
        <SubmitButton>Submit request</SubmitButton>
        <Button type="button" kind={"secondary"} onClick={onCancel}>
          Cancel
        </Button>
      </Box>
    </Form>
  );
}

export { TopicSchemaRequest };
