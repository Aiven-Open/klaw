import {
  useForm,
  Form,
  NativeSelect,
  SubmitButton,
  Textarea,
} from "src/app/components/Form";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Environment,
  getSchemaRegistryEnvironments,
} from "src/domain/environment";
import {
  TopicRequestFormSchema,
  topicRequestFormSchema,
} from "src/app/features/topics/schema-request/schemas/topic-schema-request-form";
import { TopicSchema } from "src/app/features/topics/schema-request/components/TopicSchema";
import { Alert, Box, Button } from "@aivenio/aquarium";
import { createSchemaRequest, SchemaRequest } from "src/domain/schema-request";
import { useNavigate } from "react-router-dom";
import { parseErrorMsg } from "src/services/mutation-utils";
import { getTopicNames, TopicNames } from "src/domain/topic";

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

  const navigate = useNavigate();
  const form = useForm<TopicRequestFormSchema>({
    schema: topicRequestFormSchema,
    defaultValues: {
      topicname: topicName,
      schemafull: props.schemafullValueForTest || undefined,
    },
  });

  useQuery<TopicNames, Error>(["topic-names"], {
    queryFn: () => getTopicNames({ onlyMyTeamTopics: true }),
    keepPreviousData: true,
    onSuccess: (data) => {
      const topicExists = data?.includes(topicName);
      if (!topicExists) {
        navigate("/topics");
      }
    },
  });

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >({
    queryKey: ["schemaRegistryEnvironments"],
    queryFn: () => getSchemaRegistryEnvironments(),
  });

  const schemaRequestMutation = useMutation({
    mutationFn: (schemaRequestParams: SchemaRequest) =>
      createSchemaRequest(schemaRequestParams),
  });

  if (schemaRequestMutation.isSuccess) {
    const params = new URLSearchParams({
      reqsType: "created",
      schemaCreated: "true",
    });
    navigate(`/mySchemaRequests?${params.toString()}`);
  }

  function onSubmitForm(userInput: TopicRequestFormSchema) {
    schemaRequestMutation.mutate(userInput);
  }

  function onCancel() {
    form.reset();
    navigate(-1);
  }

  return (
    <Box style={{ maxWidth: 1200 }}>
      {schemaRequestMutation.isError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert
            description={parseErrorMsg(schemaRequestMutation.error)}
            type="warning"
          ></Alert>
        </Box>
      )}
      <Form
        {...form}
        ariaLabel={"Request a new schema"}
        onSubmit={onSubmitForm}
      >
        <NativeSelect<TopicRequestFormSchema>
          name={"topicname"}
          labelText={"Topic name"}
          defaultValue={topicName}
          readOnly={true}
          aria-readonly={true}
        >
          <option value={topicName}>{topicName}</option>
        </NativeSelect>

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
              Please select
            </option>
            {environments.map((env) => {
              return (
                <option key={env.id} value={env.id}>
                  {env.name}
                </option>
              );
            })}
          </NativeSelect>
        )}

        <TopicSchema
          name={"schemafull"}
          required={!props.schemafullValueForTest}
        />

        <Textarea name={"remarks"} labelText={"Enter a message for approval"} />

        <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
          <SubmitButton>Submit request</SubmitButton>
          <Button type="button" kind={"secondary"} onClick={onCancel}>
            Cancel
          </Button>
        </Box>
      </Form>
    </Box>
  );
}

export { TopicSchemaRequest };
