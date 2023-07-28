import {
  Alert,
  Box,
  Button,
  Divider,
  Input,
  Option,
  useToast,
} from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import isEqual from "lodash/isEqual";
import { useEffect, useState } from "react";
import { SubmitHandler } from "react-hook-form";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { Dialog } from "src/app/components/Dialog";
import {
  Form,
  NativeSelect,
  SubmitButton,
  TextInput,
  Textarea,
  useForm,
} from "src/app/components/Form";
import AdvancedConfiguration from "src/app/features/topics/request/components/AdvancedConfiguration";
import SelectOrNumberInput from "src/app/features/topics/request/components/SelectOrNumberInput";
import type { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";
import formSchema from "src/app/features/topics/request/form-schemas/topic-request-form";
import { generateTopicNameDescription } from "src/app/features/topics/request/utils";
import { Routes } from "src/app/router_utils";
import { Environment } from "src/domain/environment";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment/environment-api";
import {
  TopicDetailsPerEnv,
  editTopic,
  getTopicDetailsPerEnv,
} from "src/domain/topic";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

function TopicEditRequest() {
  const { topicName } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();

  const env = searchParams.get("env");

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const { data: environments, isFetched: environmentIsFetched } = useQuery<
    Environment[],
    HTTPError
  >(
    ["getAllEnvironmentsForTopicAndAcl", env],
    getAllEnvironmentsForTopicAndAcl,
    {
      select: (environments) => environments?.filter(({ id }) => id === env),
    }
  );

  const { data: topicDetailsForEnv, isFetched: topicDetailsForEnvIsFetched } =
    useQuery<TopicDetailsPerEnv, HTTPError>(
      ["getTopicDetailsPerEnv", topicName, env],
      {
        queryFn: () =>
          getTopicDetailsPerEnv({
            topicname: topicName || "",
            envSelected: env || "",
          }),
      }
    );
  const currentEnvironment =
    environments === undefined ? undefined : environments[0];

  const form = useForm<Schema>({
    schema: formSchema,
    defaultValues: {
      environment: undefined,
      topicpartitions: undefined,
      replicationfactor: undefined,
      topicname: topicName,
      remarks: "",
      description: "",
      advancedConfiguration: "{\n}",
    },
  });

  // Handle errors when environment or topic does not exist
  useEffect(() => {
    if (environmentIsFetched && currentEnvironment === undefined) {
      navigate(`/topic/${topicName}`, { replace: true });
      toast({
        message: `No environment was found with ID ${env}`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }
    if (
      environmentIsFetched &&
      topicDetailsForEnvIsFetched &&
      !topicDetailsForEnv?.topicExists
    ) {
      navigate(Routes.TOPICS, { replace: true });
      toast({
        message: `No topic was found with name ${topicName}`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }
  }, [
    environmentIsFetched,
    currentEnvironment,
    topicDetailsForEnvIsFetched,
    topicDetailsForEnv,
  ]);

  // Initialize form with default values
  useEffect(() => {
    if (topicDetailsForEnvIsFetched && topicDetailsForEnv?.topicExists) {
      const defaultAdvancedTopicConfiguration = topicDetailsForEnv.topicContents
        ?.advancedTopicConfiguration
        ? JSON.stringify(
            topicDetailsForEnv.topicContents?.advancedTopicConfiguration
          )
        : "{\n}";
      form.reset({
        environment: currentEnvironment,
        topicpartitions: String(
          topicDetailsForEnv.topicContents?.noOfPartitions
        ),
        replicationfactor: String(
          topicDetailsForEnv.topicContents?.noOfReplicas
        ),
        topicname: topicName,
        remarks: "",
        description: topicDetailsForEnv.topicContents?.description || "",
        advancedConfiguration: defaultAdvancedTopicConfiguration,
      });
    }
  }, [
    environmentIsFetched,
    topicDetailsForEnvIsFetched,
    currentEnvironment,
    topicDetailsForEnv,
    topicName,
  ]);

  const {
    mutateAsync: edit,
    isLoading: editIsLoading,
    isError: editIsError,
    error: editError,
  } = useMutation(editTopic, {
    onSuccess: () => {
      navigate(-1);
      toast({
        message: "Topic update request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  const onEditSubmit: SubmitHandler<Schema> = (data) => {
    if (isEqual(data, form.formState.defaultValues)) {
      toast({
        message: "No changes were made to the topic.",
        position: "bottom-left",
        variant: "default",
      });
      return;
    }
    edit(data);
  };

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  return (
    <>
      {cancelDialogVisible && (
        <Dialog
          title={`Cancel topic update request?`}
          primaryAction={{
            text: `Cancel request`,
            onClick: () => cancelRequest(),
          }}
          secondaryAction={{
            text: `Continue with request`,
            onClick: () => setCancelDialogVisible(false),
          }}
          type={"warning"}
        >
          Do you want to cancel this request? The data added will be lost.
        </Dialog>
      )}
      {editIsError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert type="error">{parseErrorMsg(editError)}</Alert>
        </Box>
      )}
      <Form
        {...form}
        ariaLabel={"Request topic update"}
        onSubmit={onEditSubmit}
      >
        <Box width={"full"}>
          {currentEnvironment !== undefined ? (
            <NativeSelect
              name="environment"
              labelText={"Environment (read-only)"}
              readOnly
            >
              <Option
                key={currentEnvironment.name}
                value={currentEnvironment.name}
              >
                {currentEnvironment.name}
              </Option>
            </NativeSelect>
          ) : (
            <NativeSelect.Skeleton />
          )}
        </Box>
        <Box paddingY={"l1"}>
          <Divider />
        </Box>
        <TextInput<Schema>
          name={"topicname"}
          labelText="Topic name (read-only)"
          placeholder={generateTopicNameDescription(currentEnvironment?.params)}
          readOnly
        />
        <Box.Flex gap={"l1"}>
          <Box grow={1} width={"1/2"}>
            {currentEnvironment !== undefined ? (
              <SelectOrNumberInput
                name={"topicpartitions"}
                label={"Topic partitions"}
                max={currentEnvironment.params?.maxPartitions}
                required={true}
              />
            ) : (
              <Input.Skeleton />
            )}
          </Box>
          <Box grow={1} width={"1/2"}>
            {currentEnvironment !== undefined ? (
              <SelectOrNumberInput
                name={"replicationfactor"}
                label={"Replication factor"}
                max={currentEnvironment.params?.maxRepFactor}
                required={true}
              />
            ) : (
              <Input.Skeleton />
            )}
          </Box>
        </Box.Flex>

        <Box paddingY={"l1"}>
          <Divider />
        </Box>
        <AdvancedConfiguration name={"advancedConfiguration"} />

        <Box paddingY={"l1"}>
          <Divider />
        </Box>
        <Box.Flex gap={"l1"}>
          <Box grow={1} width={"1/2"}>
            <Textarea<Schema>
              name="description"
              labelText="Description"
              rows={5}
              required={true}
            />
          </Box>
          <Box grow={1} width={"1/2"}>
            <Textarea<Schema>
              name="remarks"
              labelText="Message for approval"
              rows={5}
            />
          </Box>
        </Box.Flex>

        <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
          <SubmitButton>Submit update request</SubmitButton>
          <Button
            type="button"
            kind={"secondary"}
            onClick={
              form.formState.isDirty
                ? () => setCancelDialogVisible(true)
                : () => cancelRequest()
            }
            disabled={editIsLoading}
          >
            Cancel
          </Button>
        </Box>
      </Form>
    </>
  );
}

export default TopicEditRequest;
