import { useEffect } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { FieldErrorsImpl, SubmitHandler } from "react-hook-form";
import { Alert, Box, Divider, Flexbox, FlexboxItem } from "@aivenio/aquarium";
import {
  Form,
  SubmitButton,
  Textarea,
  TextInput,
  useForm,
  NativeSelect,
  ComplexNativeSelect,
} from "src/app/components/Form";
import formSchema, {
  useExtendedFormValidationAndTriggers,
} from "src/app/features/topics/request/schemas/topic-request-form";
import SelectOrNumberInput from "src/app/features/topics/request/components/SelectOrNumberInput";
import type { Schema } from "src/app/features/topics/request/schemas/topic-request-form";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetEnvironmentsForTeam } from "src/domain/environment/environment-api.msw";
import { Environment } from "src/domain/environment";
import { getEnvironmentsForTeam } from "src/domain/environment/environment-api";
import AdvancedConfiguration from "src/app/features/topics/request/components/AdvancedConfiguration";
import { requestTopic } from "src/domain/topic/topic-api";
import { mockRequestTopic } from "src/domain/topic/topic-api.msw";
import { Navigate } from "react-router-dom";
import { parseErrorMsg } from "src/services/mutation-utils";
import { createTopicRequestPayload } from "src/app/features/topics/request/utils";

const mockedData = [
  createMockEnvironmentDTO({
    name: "DEV",
    id: "2",
    maxPartitions: undefined,
    maxReplicationFactor: undefined,
    defaultPartitions: "2",
    defaultReplicationFactor: "2",
    topicprefix: "dev-",
  }),
  createMockEnvironmentDTO({
    name: "TST",
    id: "1",
    maxPartitions: "6",
    maxReplicationFactor: "2",
    defaultPartitions: "3",
    defaultReplicationFactor: "2",
    topicsuffix: "_TEST",
  }),
  createMockEnvironmentDTO({
    name: "PROD",
    id: "3",
    maxPartitions: "16",
    maxReplicationFactor: "3",
    defaultPartitions: undefined,
    defaultReplicationFactor: undefined,
  }),
];

function TopicRequest() {
  useEffect(() => {
    // Remove these mock alltogether when we are happy with the functionality.
    // With mocks it is easier to demonstrate the behaviour without requesting
    // dummy topics.
    if (window.msw !== undefined) {
      mockGetEnvironmentsForTeam({
        mswInstance: window.msw,
        response: { data: mockedData },
      });

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const creationOK = { data: { data: { status: "200 OK" } }, status: 200 };
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const creationFailed = {
        data: { message: "Topic with such name already exists!" },
        status: 400,
      };
      mockRequestTopic({
        mswInstance: window.msw,
        response: creationOK,
      });
    }
  }, []);
  const { data: environments } = useQuery<Environment[], Error>(
    ["environments-for-team"],
    getEnvironmentsForTeam
  );
  const defaultValues = Array.isArray(environments)
    ? {
        environment: undefined,
        topicpartitions: "2",
        replicationfactor: "1",
        topicname: "",
        remarks: "",
        description: "",
        advancedConfiguration: "{\n}",
      }
    : undefined;

  const form = useForm<Schema>({
    schema: formSchema,
    defaultValues,
  });

  const { mutate, isLoading, isSuccess, isError, error } =
    useMutation(requestTopic);
  const onSubmit: SubmitHandler<Schema> = (data) =>
    mutate(createTopicRequestPayload(data));

  const [selectedEnvironment] = form.getValues(["environment"]);
  useExtendedFormValidationAndTriggers(form, {
    isInitialized: defaultValues !== undefined,
  });

  if (isSuccess) {
    const params = new URLSearchParams({
      reqsType: "created",
      topicCreated: "true",
    });
    return <Navigate to={`myTopicRequests?${params.toString()}`} />;
  }

  return (
    <Box style={{ maxWidth: 1200 }}>
      {isError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert description={parseErrorMsg(error)} type="warning"></Alert>
        </Box>
      )}
      <Form {...form} onSubmit={onSubmit} onError={onError}>
        <Box width={"full"}>
          {Array.isArray(environments) ? (
            <ComplexNativeSelect<Schema, Environment>
              name="environment"
              labelText={"Environment"}
              placeholder={"-- Select Environment --"}
              options={environments}
              identifierValue={"id"}
              identifierName={"name"}
            />
          ) : (
            <NativeSelect.Skeleton></NativeSelect.Skeleton>
          )}
        </Box>
        <Box>
          <Box paddingY={"l1"}>
            <Divider />
          </Box>
          <TextInput<Schema>
            name={"topicname"}
            labelText="Topic name"
            placeholder="e.g. my-topic"
            required={true}
          />
          <Box component={Flexbox} gap={"l1"}>
            <Box component={FlexboxItem} grow={1} width={"1/2"}>
              <SelectOrNumberInput
                name={"topicpartitions"}
                label={"Topic partitions"}
                max={selectedEnvironment?.maxPartitions}
                required={true}
              />
            </Box>
            <Box component={FlexboxItem} grow={1} width={"1/2"}>
              <SelectOrNumberInput
                name={"replicationfactor"}
                label={"Replication factor"}
                max={selectedEnvironment?.maxReplicationFactor}
                required={true}
              />
            </Box>
          </Box>
        </Box>
        <Box>
          <Box paddingY={"l1"}>
            <Divider />
          </Box>
          <AdvancedConfiguration name={"advancedConfiguration"} />
        </Box>

        <Box>
          <Box paddingY={"l1"}>
            <Divider />
          </Box>
          <Box component={Flexbox} gap={"l1"}>
            <Box component={FlexboxItem} grow={1} width={"1/2"}>
              <Textarea<Schema>
                name="description"
                labelText="Description"
                rows={5}
                required={true}
              />
            </Box>
            <Box component={FlexboxItem} grow={1} width={"1/2"}>
              {" "}
              <Textarea<Schema>
                name="remarks"
                labelText="Message for approval"
                rows={5}
              />
            </Box>
          </Box>
        </Box>

        <SubmitButton loading={isLoading}>Request topic</SubmitButton>
      </Form>
    </Box>
  );

  function onError(err: Partial<FieldErrorsImpl<Schema>>) {
    console.log("Form error", err);
  }
}

export default TopicRequest;
