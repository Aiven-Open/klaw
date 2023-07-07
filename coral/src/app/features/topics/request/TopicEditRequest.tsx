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

  const { data: environments } = useQuery<Environment[], HTTPError>(
    ["getEnvironmentsForTopicRequest"],
    {
      queryFn: () => getAllEnvironmentsForTopicAndAcl(),
    }
  );

  const { data: topicDetailsForSourceEnv } = useQuery<
    TopicDetailsPerEnv,
    HTTPError
  >(["getTopicDetailsPerEnv", topicName, env], {
    queryFn: () =>
      getTopicDetailsPerEnv({
        topicname: topicName || "",
        envSelected: env || "",
      }),
  });

  const currentEnvironment = environments?.find(({ id }) => id === env);

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

  useEffect(() => {
    if (
      currentEnvironment !== undefined &&
      topicDetailsForSourceEnv !== undefined
    ) {
      form.setValue("environment", currentEnvironment);
      form.setValue(
        "topicpartitions",
        String(topicDetailsForSourceEnv.topicContents?.noOfPartitions)
      );
      form.setValue(
        "replicationfactor",
        String(topicDetailsForSourceEnv.topicContents?.noOfReplicas)
      );

      if (topicDetailsForSourceEnv.topicContents?.description !== undefined) {
        form.setValue(
          "description",
          topicDetailsForSourceEnv.topicContents.description
        );
      }

      if (
        topicDetailsForSourceEnv.topicContents?.advancedTopicConfiguration !==
        undefined
      ) {
        form.setValue(
          "advancedConfiguration",
          JSON.stringify(
            topicDetailsForSourceEnv.topicContents.advancedTopicConfiguration
          )
        );
      }
    }
  }, [currentEnvironment, topicDetailsForSourceEnv]);

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
    if (isEqual(data, form.watch())) {
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
              labelText={"Environment"}
              required
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
          labelText="Topic name"
          placeholder={generateTopicNameDescription(currentEnvironment?.params)}
          required={true}
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
