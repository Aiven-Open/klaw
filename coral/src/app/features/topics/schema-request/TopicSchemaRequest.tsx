import { Alert, Box, Button } from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Dialog } from "src/app/components/Dialog";
import {
  Form,
  NativeSelect,
  SubmitButton,
  Textarea,
  useForm,
} from "src/app/components/Form";
import { TopicSchema } from "src/app/features/topics/schema-request/components/TopicSchema";
import {
  TopicRequestFormSchema,
  topicRequestFormSchema,
} from "src/app/features/topics/schema-request/form-schemas/topic-schema-request-form";
import {
  Environment,
  getEnvironmentsForSchemaRequest,
} from "src/domain/environment";
import { createSchemaRequest } from "src/domain/schema-request";
import { TopicNames, getTopicNames } from "src/domain/topic";
import { parseErrorMsg } from "src/services/mutation-utils";

type TopicSchemaRequestProps = {
  topicName?: string;
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
  const [searchParams] = useSearchParams();

  const [presetEnvironment, setPresetEnvironment] = useState<
    string | undefined
  >();

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);
  const [successModalOpen, setSuccessModalOpen] = useState(false);

  const navigate = useNavigate();
  const form = useForm<TopicRequestFormSchema>({
    schema: topicRequestFormSchema,
    defaultValues: {
      topicname: topicName,
      schemafull: props.schemafullValueForTest || undefined,
      environment: presetEnvironment,
    },
  });

  const { data: topicNames, isLoading: topicNamesIsLoading } = useQuery<
    TopicNames,
    Error
  >(["topic-names"], {
    queryFn: () =>
      getTopicNames({
        onlyMyTeamTopics: true,
      }),
    keepPreviousData: true,
    onSuccess: (data) => {
      if (topicName === undefined) {
        return;
      }

      const topicExists = data?.includes(topicName);
      if (!topicExists) {
        navigate(-1);
      }
    },
  });

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >({
    queryKey: ["schemaRegistryEnvironments"],
    queryFn: () => getEnvironmentsForSchemaRequest(),
    onSuccess: (environments) => {
      const currentEnv = searchParams.get("env");
      if (currentEnv) {
        const isValidEnv =
          environments.find((env) => currentEnv === env.id) !== undefined;
        if (!isValidEnv) {
          navigate(-1);
        } else {
          console.log("currentEnv", currentEnv);
          setPresetEnvironment(currentEnv);
          form.setValue("environment", currentEnv);
        }
      }
    },
  });

  const schemaRequestMutation = useMutation(createSchemaRequest, {
    onSuccess: () => {
      setSuccessModalOpen(true);
      setTimeout(() => {
        redirectToMyRequests();
      }, 5 * 1000);
    },
  });

  function redirectToMyRequests() {
    navigate("/requests/schemas?status=CREATED");
  }

  function onSubmitForm(userInput: TopicRequestFormSchema) {
    schemaRequestMutation.mutate(userInput);
  }

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  return (
    <>
      {!topicNamesIsLoading && topicNames === undefined && (
        <Box marginBottom={"l1"}>
          {" "}
          <Alert type="error">Could not fetch topic names.</Alert>
        </Box>
      )}
      {!environmentsIsLoading && environments === undefined && (
        <Box marginBottom={"l1"}>
          <Alert type="error">Could not fetch environments.</Alert>
        </Box>
      )}
      {successModalOpen && (
        <Dialog
          title={"Schema request successful!"}
          primaryAction={{
            text: "Continue",
            onClick: redirectToMyRequests,
          }}
          type={"confirmation"}
        >
          Redirecting to My team&apos;s request page shortly. Select
          &quot;Continue&quot; for an immediate redirect.
        </Dialog>
      )}
      <Box>
        {schemaRequestMutation.isError && (
          <Box marginBottom={"l1"} role="alert">
            <Alert type="error">
              {parseErrorMsg(schemaRequestMutation.error)}
            </Alert>
          </Box>
        )}
        <Form
          {...form}
          ariaLabel={"Request a new schema"}
          onSubmit={onSubmitForm}
        >
          {topicNamesIsLoading || topicNames === undefined ? (
            <div data-testid={"topicNames-select-loading"}>
              <NativeSelect.Skeleton />
            </div>
          ) : (
            <NativeSelect<TopicRequestFormSchema>
              name={"topicname"}
              labelText={"Topic name"}
              readOnly={topicName !== undefined}
            >
              {topicNames.map((topic) => {
                return (
                  <option key={topic} value={topic}>
                    {topic}
                  </option>
                );
              })}
            </NativeSelect>
          )}

          {environmentsIsLoading || environments === undefined ? (
            <div data-testid={"environments-select-loading"}>
              <NativeSelect.Skeleton />
            </div>
          ) : (
            <NativeSelect<TopicRequestFormSchema>
              name={"environment"}
              labelText={"Environment"}
              placeholder={
                presetEnvironment ? undefined : "-- Please select --"
              }
              readOnly={presetEnvironment !== undefined}
              required={true}
            >
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

          <Textarea
            name={"remarks"}
            labelText={"Enter a message for approval"}
          />

          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton>Submit request</SubmitButton>
            <Button
              type="button"
              kind={"secondary"}
              onClick={
                form.formState.isDirty
                  ? () => setCancelDialogVisible(true)
                  : () => cancelRequest()
              }
            >
              Cancel
            </Button>
          </Box>
        </Form>
      </Box>
      {cancelDialogVisible && (
        <Dialog
          title={"Cancel schema request?"}
          primaryAction={{
            text: "Cancel request",
            onClick: () => cancelRequest(),
          }}
          secondaryAction={{
            text: "Continue with request",
            onClick: () => setCancelDialogVisible(false),
          }}
          type={"warning"}
        >
          Do you want to cancel this request? The data added will be lost.
        </Dialog>
      )}
    </>
  );
}

export { TopicSchemaRequest };
