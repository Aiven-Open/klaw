import {
  Alert,
  RadioButton as BaseRadioButton,
  Box,
  Button,
  useToast,
  Spacing,
} from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { AsyncNativeSelectWrapper } from "src/app/components/AsyncNativeSelectWrapper";
import { Dialog } from "src/app/components/Dialog";
import {
  Checkbox,
  Form,
  NativeSelect,
  RadioButtonGroup,
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
  getAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import {
  SchemaRequest,
  requestSchemaCreation,
} from "src/domain/schema-request";
import { TopicNames, getTopicNames } from "src/domain/topic";
import { KlawApiError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

type TopicSchemaRequestProps = {
  presetTopicName?: string;
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
  const { presetTopicName } = props;
  const [searchParams] = useSearchParams();
  const presetSchemaEnvironment = searchParams.get("schemaEnv") || undefined;
  const presetSchemaType = (searchParams.get("schemaType") ??
    "AVRO") as SchemaRequest["schemaType"];

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);
  const [isValidationError, setIsValidationError] = useState(false);
  // set when a "schemaEnv" param was given but no Kafka env has it as associated env
  const [noKafkaEnvForSchemaEnv, setNoKafkaEnvForSchemaEnv] = useState(false);

  const navigate = useNavigate();
  const toast = useToast();

  const hasPresetTopicName = presetTopicName !== undefined;
  const hasPresetSchemaEnvironment =
    presetSchemaEnvironment !== null &&
    presetSchemaEnvironment !== undefined &&
    presetSchemaEnvironment.length > 0;

  const form = useForm<TopicRequestFormSchema>({
    schema: topicRequestFormSchema,
    defaultValues: {
      topicname: presetTopicName,
      schemafull: props.schemafullValueForTest || undefined,
      schemaType: presetSchemaType,
    },
  });

  const {
    data: topicNames,
    isLoading: isLoadingTopicNames,
    isError: isErrorTopicNames,
    error: errorTopicNames,
  } = useQuery<TopicNames, Error>(["topic-names"], {
    queryFn: () =>
      getTopicNames({
        onlyMyTeamTopics: true,
      }),
  });

  //Topic guard: redirect back when the preset topic is not one of the users team topics
  useEffect(() => {
    if (presetTopicName === undefined || topicNames === undefined) {
      return;
    }

    const topicExists = topicNames.includes(presetTopicName);
    if (!topicExists) {
      navigate(-1);
    }
  }, [topicNames, presetTopicName]);

  const {
    data: environments,
    isLoading: isLoadingEnvironments,
    isError: isErrorEnvironments,
    error: errorEnvironments,
  } = useQuery<Environment[], Error>({
    queryKey: ["getEnvs"],
    queryFn: () => getAllEnvironmentsForTopicAndAcl(),
    // not every Kafka Environment has an associated env for schemas,
    // we only want to show those who do.
    select: (kafkaEnvironments) =>
      kafkaEnvironments.filter((env) => env.associatedEnv),
  });

  // Get the associated kafka env for the "schemaEnv" search param
  const matchedKafkaEnv = useMemo(() => {
    if (presetSchemaEnvironment === undefined || environments === undefined) {
      return undefined;
    }
    return environments.find(
      (env) =>
        presetSchemaEnvironment === env.id ||
        presetSchemaEnvironment === env.name ||
        presetSchemaEnvironment === env.associatedEnv?.id ||
        presetSchemaEnvironment === env.associatedEnv?.name
    );
  }, [environments, presetSchemaEnvironment]);

  //Schema env guard: redirect back when the preset schema env is not in the list
  useEffect(() => {
    if (
      presetSchemaEnvironment !== undefined &&
      environments !== undefined &&
      matchedKafkaEnv === undefined
    ) {
      setNoKafkaEnvForSchemaEnv(true);
    }
  }, [environments, presetSchemaEnvironment, matchedKafkaEnv]);

  // The form expect the Kafka env id
  useEffect(() => {
    if (matchedKafkaEnv !== undefined) {
      form.setValue("environment", matchedKafkaEnv.id);
    }
  }, [matchedKafkaEnv, form]);

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
      const validationErrorMessages = [
        "schema is not compatible",
        "unable to validate schema compatibility",
      ];

      const matchedError = validationErrorMessages.some((errorMessage) => {
        return error?.message
          .toLowerCase()
          .includes(errorMessage.toLowerCase());
      });

      setIsValidationError(matchedError);
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
      <Spacing gap={"l2"}>
        {schemaRequestMutation.isError && !isValidationError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">
              {parseErrorMsg(schemaRequestMutation.error)}
            </Alert>
          </Box>
        )}
        {noKafkaEnvForSchemaEnv && (
          <Alert type="warning">
            The schema environment &quot;
            {presetSchemaEnvironment}
            &quot; could not be resolved to a Kafka environment. It may not
            exist or have no associated Kafka environment. Please contact your
            admin.
          </Alert>
        )}

        <Form
          {...form}
          ariaLabel={"Request a new schema"}
          onSubmit={onSubmitForm}
        >
          <AsyncNativeSelectWrapper
            entity={"topic name"}
            isLoading={isLoadingTopicNames}
            isError={isErrorTopicNames}
            error={errorTopicNames}
          >
            <NativeSelect<TopicRequestFormSchema>
              name={"topicname"}
              labelText={
                hasPresetTopicName ? "Topic name (read-only)" : "Topic name"
              }
              required={!hasPresetTopicName}
              readOnly={hasPresetTopicName}
              placeholder={"-- Please select --"}
            >
              {topicNames?.map((topic) => {
                return (
                  <option key={topic} value={topic}>
                    {topic}
                  </option>
                );
              })}
            </NativeSelect>
          </AsyncNativeSelectWrapper>
          <AsyncNativeSelectWrapper
            entity={"Environments"}
            isLoading={isLoadingEnvironments}
            isError={isErrorEnvironments}
            error={errorEnvironments}
          >
            <NativeSelect<TopicRequestFormSchema>
              name={"environment"}
              labelText={
                hasPresetSchemaEnvironment
                  ? "Environment (read-only)"
                  : "Environment"
              }
              placeholder={"-- Please select --"}
              readOnly={hasPresetSchemaEnvironment}
              required={!presetSchemaEnvironment}
            >
              {environments?.map((env) => {
                return (
                  <option key={env.id} value={env.id}>
                    {env.name}
                  </option>
                );
              })}
            </NativeSelect>
          </AsyncNativeSelectWrapper>
          <RadioButtonGroup
            name="schemaType"
            labelText={"Schema type"}
            required
          >
            <BaseRadioButton value="AVRO">AVRO</BaseRadioButton>
            <BaseRadioButton value="JSON">JSON</BaseRadioButton>
          </RadioButtonGroup>
          <TopicSchema
            name={"schemafull"}
            required={!props.schemafullValueForTest}
            schemaType={form.watch("schemaType")}
          />
          <Textarea
            name={"remarks"}
            labelText={"Message for approval"}
            placeholder="Comments about this request for the approver."
          />
          {isValidationError && (
            <>
              <Box marginBottom={"l1"}>
                <Alert type={"warning"}>Uploaded schema appears invalid.</Alert>
              </Box>

              <Box marginBottom={"l2"}>
                {/*We only allow users to use the forceRegister option when the promotion request failed*/}
                {/*And the failure is because of a schema compatibility issue*/}
                <Checkbox<TopicRequestFormSchema>
                  name={"forceRegister"}
                  caption={
                    <>
                      Warning: This will override standard validation process of
                      the schema registry.{" "}
                      <a
                        href={
                          "https://www.klaw-project.io/docs/HowTo/schemas/Promote-a-schema/#how-does-force-register-work"
                        }
                      >
                        Learn more
                      </a>
                    </>
                  }
                >
                  Force register schema creation/changes
                </Checkbox>
              </Box>
            </>
          )}
          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton
              disabled={
                (isValidationError && !form.watch("forceRegister")) ||
                schemaRequestMutation.isLoading ||
                noKafkaEnvForSchemaEnv
              }
            >
              {isValidationError
                ? "Submit request to force register"
                : "Submit request"}
            </SubmitButton>
            <Button
              disabled={schemaRequestMutation.isLoading}
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
      </Spacing>
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
