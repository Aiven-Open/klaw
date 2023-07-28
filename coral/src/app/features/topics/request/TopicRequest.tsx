import { useMutation, useQuery } from "@tanstack/react-query";
import { FieldErrorsImpl, SubmitHandler } from "react-hook-form";
import {
  Alert,
  Box,
  Button,
  Divider,
  Flexbox,
  FlexboxItem,
  useToast,
} from "@aivenio/aquarium";
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
} from "src/app/features/topics/request/form-schemas/topic-request-form";
import SelectOrNumberInput from "src/app/features/topics/request/components/SelectOrNumberInput";
import type { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";
import { Environment } from "src/domain/environment";
import { getEnvironmentsForTopicRequest } from "src/domain/environment/environment-api";
import AdvancedConfiguration from "src/app/features/topics/request/components/AdvancedConfiguration";
import { requestTopic } from "src/domain/topic/topic-api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { generateTopicNameDescription } from "src/app/features/topics/request/utils";
import { Dialog } from "src/app/components/Dialog";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

function TopicRequest() {
  const navigate = useNavigate();
  const toast = useToast();

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const { data: environments } = useQuery<Environment[], Error>(
    ["environments-for-team"],
    getEnvironmentsForTopicRequest
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

  const { mutate, isLoading, isError, error } = useMutation(requestTopic, {
    onSuccess: () => {
      navigate("/requests/topics?status=CREATED");
      toast({
        message: "Topic request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

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
          <Box marginBottom={"l1"} role="alert">
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
              <ComplexNativeSelect<Schema, Environment>
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
            <Box component={Flexbox} gap={"l1"}>
              <Box component={FlexboxItem} grow={1} width={"1/2"}>
                <SelectOrNumberInput
                  name={"topicpartitions"}
                  label={"Topic partitions"}
                  max={selectedEnvironment?.params?.maxPartitions}
                  required={true}
                />
              </Box>
              <Box component={FlexboxItem} grow={1} width={"1/2"}>
                <SelectOrNumberInput
                  name={"replicationfactor"}
                  label={"Replication factor"}
                  max={selectedEnvironment?.params?.maxRepFactor}
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
                  labelText="Topic description"
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

          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton loading={isLoading}>Submit request</SubmitButton>
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
    </>
  );

  function onError(err: Partial<FieldErrorsImpl<Schema>>) {
    console.log("Form error", err);
  }
}

export default TopicRequest;
