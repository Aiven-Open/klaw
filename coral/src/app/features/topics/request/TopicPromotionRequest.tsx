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
  getTopicDetailsPerEnv,
  promoteTopic,
} from "src/domain/topic";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

function TopicPromotionRequest() {
  const { topicName } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();

  const sourceEnv = searchParams.get("sourceEnv");
  const targetEnv = searchParams.get("targetEnv");

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const { data: environments, isFetched: environmentsIsFetched } = useQuery<
    Environment[],
    HTTPError
  >(["getEnvironmentsForTopicRequest"], {
    queryFn: () => getAllEnvironmentsForTopicAndAcl(),
  });

  const {
    data: topicDetailsForSourceEnv,
    isFetched: topicDetailsForSourceEnvIsFetched,
  } = useQuery<TopicDetailsPerEnv, HTTPError>(
    ["getTopicDetailsPerEnv", topicName, sourceEnv],
    {
      queryFn: () =>
        getTopicDetailsPerEnv({
          topicname: topicName || "",
          envSelected: sourceEnv || "",
        }),
    }
  );

  const targetEnvironment = environments?.find(({ id }) => id === targetEnv);
  const sourceEnvironment = environments?.find(({ id }) => id === sourceEnv);

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
    if (environmentsIsFetched && targetEnvironment === undefined) {
      navigate(`/topic/${topicName}`, { replace: true });
      toast({
        message: `No target environment was found with ID ${targetEnv}`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }
    if (environmentsIsFetched && sourceEnvironment === undefined) {
      navigate(`/topic/${topicName}`, { replace: true });
      toast({
        message: `No source environment was found with ID ${sourceEnv}`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }
    if (
      environmentsIsFetched &&
      topicDetailsForSourceEnvIsFetched &&
      !topicDetailsForSourceEnv?.topicExists
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
    environmentsIsFetched,
    targetEnvironment,
    topicDetailsForSourceEnvIsFetched,
    topicDetailsForSourceEnv,
  ]);

  useEffect(() => {
    if (
      targetEnvironment !== undefined &&
      targetEnvironment.params !== undefined &&
      topicDetailsForSourceEnv !== undefined &&
      topicDetailsForSourceEnv.topicContents !== undefined
    ) {
      form.reset({
        environment: targetEnvironment,
        topicpartitions: String(targetEnvironment?.params.defaultPartitions),
        replicationfactor: String(targetEnvironment?.params.defaultRepFactor),
        topicname: topicName,
        remarks: "",
        description: topicDetailsForSourceEnv.topicContents.description,
        advancedConfiguration: JSON.stringify(
          topicDetailsForSourceEnv.topicContents.advancedTopicConfiguration
        ),
      });
    }
  }, [targetEnvironment, topicDetailsForSourceEnv]);

  const {
    mutate: promote,
    isLoading: promoteIsLoading,
    isError: promoteIsError,
    error: promoteError,
  } = useMutation(promoteTopic, {
    onSuccess: () => {
      navigate(-1);
      toast({
        message: "Topic promotion request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  const onPromoteSubmit: SubmitHandler<Schema> = (data) => promote(data);

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  return (
    <>
      {cancelDialogVisible && (
        <Dialog
          title={`Cancel topic promotion request?`}
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
      {promoteIsError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert type="error">{parseErrorMsg(promoteError)}</Alert>
        </Box>
      )}
      <Form
        {...form}
        ariaLabel={"Request topic promotion"}
        onSubmit={onPromoteSubmit}
      >
        <Box width={"full"}>
          {targetEnvironment !== undefined ? (
            <NativeSelect
              name="environment"
              labelText={"Environment"}
              required
              readOnly
            >
              <Option
                key={targetEnvironment.name}
                value={targetEnvironment.name}
              >
                {targetEnvironment.name}
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
          labelText="Topic name"
          placeholder={generateTopicNameDescription(targetEnvironment?.params)}
          required={true}
          readOnly
        />
        <Box gap={"l1"}>
          <Box grow={1} width={"1/2"}>
            {targetEnvironment !== undefined ? (
              <SelectOrNumberInput
                name={"topicpartitions"}
                label={"Topic partitions"}
                max={targetEnvironment.params?.maxPartitions}
                required={true}
              />
            ) : (
              <Input.Skeleton />
            )}
          </Box>
          <Box grow={1} width={"1/2"}>
            {targetEnvironment !== undefined ? (
              <SelectOrNumberInput
                name={"replicationfactor"}
                label={"Replication factor"}
                max={targetEnvironment.params?.maxRepFactor}
                required={true}
              />
            ) : (
              <Input.Skeleton />
            )}
          </Box>
        </Box>

        <Box paddingY={"l1"}>
          <Divider />
        </Box>
        <AdvancedConfiguration name={"advancedConfiguration"} />

        <Box paddingY={"l1"}>
          <Divider />
        </Box>
        <Box gap={"l1"}>
          <Box grow={1} width={"1/2"}>
            <Textarea<Schema>
              name="description"
              labelText="Description"
              rows={5}
              required={true}
              readOnly
            />
          </Box>
          <Box grow={1} width={"1/2"}>
            <Textarea<Schema>
              name="remarks"
              labelText="Message for approval"
              rows={5}
            />
          </Box>
        </Box>

        <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
          <SubmitButton loading={promoteIsLoading}>
            Submit promotion request
          </SubmitButton>
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
    </>
  );
}

export default TopicPromotionRequest;
