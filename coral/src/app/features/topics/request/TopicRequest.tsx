import { useMutation, useQuery } from "@tanstack/react-query";
import { FieldErrorsImpl, SubmitHandler } from "react-hook-form";
import { Alert, Box, Button, Divider, useToast } from "@aivenio/aquarium";
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
  EnvironmentForTopicForm,
  useExtendedFormValidationAndTriggers,
} from "src/app/features/topics/request/form-schemas/topic-request-form";
import SelectOrNumberInput from "src/app/features/topics/request/components/SelectOrNumberInput";
import type { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";
import { getEnvironmentsForTopicRequest } from "src/domain/environment/environment-api";
import AdvancedConfiguration from "src/app/features/topics/request/components/AdvancedConfiguration";
import { requestTopicCreation } from "src/domain/topic/topic-api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { generateTopicNameDescription } from "src/app/features/topics/request/utils";
import { Dialog } from "src/app/components/Dialog";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import isString from "lodash/isString";

function parseNumberOrUndefined(value: string | undefined): number | undefined {
  return isString(value) ? parseInt(value, 10) : undefined;
}

function TopicRequest() {
  const navigate = useNavigate();
  const toast = useToast();

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const { data: environments } = useQuery<EnvironmentForTopicForm[], Error>(
    ["environments-for-team"],
    {
      queryFn: getEnvironmentsForTopicRequest,
      select: (data) =>
        data.map(({ name, id, params }) => ({ name, id, params })),
    }
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

  const { mutate, isLoading, isError, error } = useMutation(
    requestTopicCreation,
    {
      onSuccess: () => {
        navigate("/requests/topics?status=CREATED");
        toast({
          message: "Topic request successfully created",
          position: "bottom-left",
          variant: "default",
        });
      },
    }
  );

  const onSubmit: SubmitHandler<Schema> = (data) => mutate(data);

  const [selectedEnvironment] = form.getValues(["environment"]);
  useExtendedFormValidationAndTriggers(form, {
    isInitialized: defaultValues !== undefined,
  });

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  return (
    <>
      {cancelDialogVisible && (
        <Dialog
          title={"Cancel topic request?"}
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
      <Box>
        {isError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">{parseErrorMsg(error)}</Alert>
          </Box>
        )}
        <Form
          {...form}
          ariaLabel={"Request a new topic"}
          onSubmit={onSubmit}
          onError={onError}
        >
          <Box width={"full"}>
            {Array.isArray(environments) ? (
              <ComplexNativeSelect<Schema, EnvironmentForTopicForm>
                name="environment"
                labelText={"Environment"}
                placeholder={"-- Please select --"}
                options={environments}
                identifierValue={"id"}
                identifierName={"name"}
                required
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
              placeholder={generateTopicNameDescription(
                selectedEnvironment?.params
              )}
              required={true}
            />
            <Box.Flex gap={"l1"}>
              <Box width={"1/2"}>
                <SelectOrNumberInput
                  name={"topicpartitions"}
                  label={"Topic partitions"}
                  max={parseNumberOrUndefined(
                    selectedEnvironment?.params.maxPartitions
                  )}
                  required={true}
                />
              </Box>
              <Box width={"1/2"}>
                <SelectOrNumberInput
                  name={"replicationfactor"}
                  label={"Replication factor"}
                  max={parseNumberOrUndefined(
                    selectedEnvironment?.params.maxRepFactor
                  )}
                  required={true}
                />
              </Box>
            </Box.Flex>
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
            <Box.Flex gap={"l1"}>
              <Box width={"1/2"}>
                <Textarea<Schema>
                  name="description"
                  labelText="Topic description"
                  rows={5}
                  required={true}
                />
              </Box>
              <Box width={"1/2"}>
                {" "}
                <Textarea<Schema>
                  name="remarks"
                  labelText="Message for approval"
                  placeholder="Comments about this request for the approver."
                  rows={5}
                />
              </Box>
            </Box.Flex>
          </Box>

          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton loading={isLoading}>Submit request</SubmitButton>
            <Button
              disabled={isLoading}
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
    </>
  );

  function onError(err: Partial<FieldErrorsImpl<Schema>>) {
    console.error("Form error", err);
  }
}

export default TopicRequest;
