import { Alert, Box, Button, useToast } from "@aivenio/aquarium";
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
  Checkbox,
} from "src/app/components/Form";
import { TopicSchema } from "src/app/features/topics/schema-request/components/TopicSchema";
import {
  TopicRequestFormSchema,
  topicRequestFormSchema,
} from "src/app/features/topics/schema-request/form-schemas/topic-schema-request-form";
import {
  Environment,
  getAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import { requestSchemaCreation } from "src/domain/schema-request";
import { TopicNames, getTopicNames } from "src/domain/topic";
import { parseErrorMsg } from "src/services/mutation-utils";
import { KlawApiError } from "src/services/api";

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
// be able to add this tests. I created na issue in GitHub related to MonacoEditor testing already.
function TopicSchemaRequest(props: TopicSchemaRequestProps) {
  const { topicName } = props;
  const [searchParams] = useSearchParams();
  const presetEnvironment = searchParams.get("env");

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);
  const [isValidationError, setIsValidationError] = useState(false);

  const navigate = useNavigate();
  const toast = useToast();

  const hasPresetTopicName = topicName !== undefined;
  const hasPresetEnvironment =
    presetEnvironment !== null &&
    presetEnvironment !== undefined &&
    presetEnvironment.length > 0;

  const form = useForm<TopicRequestFormSchema>({
    schema: topicRequestFormSchema,
    defaultValues: {
      topicname: topicName,
      schemafull: props.schemafullValueForTest || undefined,
      environment: presetEnvironment || undefined,
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
    queryKey: ["getEnvs"],
    queryFn: () => getAllEnvironmentsForTopicAndAcl(),
    // not every Kafka Environment has an associated env for schemas,
    // so we only want to show those who do.
    // We use name and ID related to the Kafka Environment in form and mutation.
    select: (kafkaEnvironments) =>
      kafkaEnvironments.filter((env) => env.associatedEnv),
    onSuccess: (environments) => {
      if (presetEnvironment) {
        const validEnv = environments.find(
          (env) =>
            presetEnvironment === env.id || presetEnvironment === env.name
        );

        // Allows to pass environment name as well as environment id as search param
        if (validEnv && isNaN(Number(presetEnvironment))) {
          form.setValue("environment", validEnv.id);
          return;
        }

        if (validEnv === undefined) {
          navigate(-1);
        }
      }
    },
  });

  const schemaRequestMutation = useMutation(requestSchemaCreation, {
    onSuccess: () => {
      setIsValidationError(false);
      navigate("/requests/schemas?status=CREATED");
      toast({
        message: "Schema request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
    onError: (error: KlawApiError | Error) => {
      setIsValidationError(error?.message.includes("Invalid Schema"));
    },
  });

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
      <Box>
        {schemaRequestMutation.isError && !isValidationError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">
              {parseErrorMsg(schemaRequestMutation.error)}
            </Alert>
          </Box>
        )}
        {isValidationError && (
          <Box marginBottom={"l1"}>
            <Alert type={"warning"}>
              The uploaded schema does not appear to be valid. You can force
              register the schema.
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
              labelText={
                hasPresetTopicName ? "Topic name (read-only)" : "Topic name"
              }
              required={!hasPresetTopicName}
              readOnly={hasPresetTopicName}
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
              labelText={
                hasPresetEnvironment ? "Environment (read-only)" : "Environment"
              }
              placeholder={"-- Please select --"}
              readOnly={hasPresetEnvironment}
              required={!presetEnvironment}
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
            labelText={"Message for approval"}
            placeholder="Comments about this request for the approver."
          />
          {isValidationError && (
            <Box marginBottom={"l2"}>
              {/*We only allow users to use the forceRegister option when the promotion request failed*/}
              {/*And the failure is because of a schema compatibility issue*/}
              <Checkbox<TopicRequestFormSchema>
                name={"forceRegister"}
                caption={
                  "Overrides some validation that the schema registry would normally do."
                }
              >
                Force register
              </Checkbox>
            </Box>
          )}
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
