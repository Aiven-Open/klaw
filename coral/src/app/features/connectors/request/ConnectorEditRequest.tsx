import {
  Alert,
  BorderBox,
  Box,
  Button,
  Grid,
  Label,
  Option,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { useMutation, useQuery } from "@tanstack/react-query";
import isEqual from "lodash/isEqual";
import { useEffect, useState } from "react";
import { SubmitHandler, Controller as _Controller } from "react-hook-form";
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
import {
  ConnectorRequestFormSchema,
  connectorRequestFormSchema,
} from "src/app/features/connectors/request/schemas/connector-request-form";
import { Routes } from "src/app/router_utils";
import {
  ConnectorDetailsForEnv,
  editConnector,
  getConnectorDetailsPerEnv,
} from "src/domain/connector";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

function ConnectorEditRequest() {
  const { connectorName } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();

  const env = searchParams.get("env");

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const form = useForm<ConnectorRequestFormSchema>({
    schema: connectorRequestFormSchema,
    defaultValues: {
      connectorName,
    },
  });

  const {
    data: connectorDetailsForEnv,
    isFetched: connectorDetailsForEnvIsFetched,
    isError: connectorDetailsForEnvIsError,
    error: connectorDetailsForEnvError,
  } = useQuery<ConnectorDetailsForEnv, HTTPError>(
    ["getConnectorDetailsPerEnv", connectorName, env],
    {
      queryFn: () =>
        getConnectorDetailsPerEnv({
          connectorName: connectorName || "",
          envSelected: env || "",
        }),
    }
  );

  // Handle errors when environment or topic does not exist
  useEffect(() => {
    if (env === null) {
      navigate(Routes.CONNECTORS, { replace: true });
      toast({
        message: `No environment provided.`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }

    if (connectorDetailsForEnvIsError) {
      navigate(Routes.CONNECTORS, { replace: true });
      toast({
        message: `Could not fetch ${connectorName}: ${parseErrorMsg(
          connectorDetailsForEnvError
        )}`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }

    if (
      connectorDetailsForEnvIsFetched &&
      !connectorDetailsForEnv?.connectorExists
    ) {
      navigate(Routes.CONNECTORS, { replace: true });
      toast({
        message: `No connector was found with name ${connectorName} in given environment ${env}.`,
        position: "bottom-left",
        variant: "danger",
      });
      return;
    }
  }, [
    connectorDetailsForEnvIsFetched,
    connectorDetailsForEnv,
    connectorDetailsForEnvIsError,
    connectorDetailsForEnvError,
  ]);

  // Initialize form with default values
  useEffect(() => {
    if (
      connectorDetailsForEnvIsFetched &&
      connectorDetailsForEnv?.connectorExists &&
      connectorDetailsForEnv.connectorContents !== undefined &&
      env !== null
    ) {
      form.reset({
        environment: env,
        remarks: "",
        description:
          connectorDetailsForEnv.connectorContents?.description || "",
        connectorConfig:
          connectorDetailsForEnv.connectorContents.connectorConfig || "{\n}",
      });
    }
  }, [
    connectorDetailsForEnv,
    connectorDetailsForEnvIsFetched,
    env,
    connectorName,
  ]);

  const {
    mutateAsync: edit,
    isLoading: editIsLoading,
    isError: editIsError,
    error: editError,
  } = useMutation(editConnector, {
    onSuccess: () => {
      navigate(-1);
      toast({
        message: "Connector update request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  const onEditSubmit: SubmitHandler<ConnectorRequestFormSchema> = (data) => {
    if (isEqual(data, { ...form.formState.defaultValues, connectorName })) {
      toast({
        message: "No changes were made to the connector.",
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

  const environmentName =
    connectorDetailsForEnv?.connectorContents?.environmentName;

  return (
    <>
      <Box>
        {editIsError && (
          <Box marginBottom={"l1"} role="alert">
            <Alert type="error">{parseErrorMsg(editError)}</Alert>
          </Box>
        )}
        <Form
          {...form}
          ariaLabel={"Request connector update"}
          onSubmit={onEditSubmit}
        >
          {environmentName !== undefined ? (
            <NativeSelect
              name="environment"
              labelText={"Environment"}
              required
              readOnly
            >
              <Option key={environmentName} value={environmentName}>
                {environmentName}
              </Option>
            </NativeSelect>
          ) : (
            <NativeSelect.Skeleton />
          )}
          <TextInput<ConnectorRequestFormSchema>
            name={"connectorName"}
            labelText={"Connector name"}
            required
            readOnly
          />

          <Label labelText="Connector configuration" required />

          <_Controller<ConnectorRequestFormSchema>
            name={"connectorConfig"}
            control={form.control}
            render={({ field, fieldState: { error } }) => {
              return (
                <>
                  <BorderBox
                    borderColor="grey-20"
                    borderWidth={1}
                    paddingY={"3"}
                    borderRadius={2}
                  >
                    <MonacoEditor
                      data-testid="connector-request-config"
                      height="200px"
                      language="json"
                      theme={"light"}
                      onChange={(value) =>
                        value !== undefined &&
                        form.setValue("connectorConfig", value, {
                          shouldValidate: true,
                        })
                      }
                      options={{
                        language: "json",
                        ariaLabel: "Connector configuration",
                        renderControlCharacters: false,
                        minimap: { enabled: false },
                        folding: false,
                        lineNumbers: "off",
                        scrollBeyondLastLine: false,
                        renderLineHighlight: "none",
                        cursorBlinking: "solid",
                        overviewRulerLanes: 0,
                        wordBasedSuggestions: false,
                        lineNumbersMinChars: 3,
                        glyphMargin: false,
                        cursorStyle: "line-thin",
                        scrollbar: {
                          useShadows: false,
                          verticalScrollbarSize: 2,
                        },
                        fixedOverflowWidgets: true,
                      }}
                      value={field.value}
                    />
                  </BorderBox>
                  <Box marginTop={"1"} marginBottom={"3"} aria-hidden={"true"}>
                    <Typography.Caption color={"error-50"}>
                      {error !== undefined ? error.message : <>&nbsp;</>}
                    </Typography.Caption>
                  </Box>
                </>
              );
            }}
          />

          <Grid colGap={"l1"} marginTop={"3"} cols={"2"}>
            <Textarea<ConnectorRequestFormSchema>
              name={"description"}
              labelText={"Connector description"}
              required
            />
            <Textarea<ConnectorRequestFormSchema>
              name={"remarks"}
              labelText={"Message for approval"}
            />
          </Grid>

          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton disabled={editIsLoading}>
              Submit update request
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
      </Box>
      {cancelDialogVisible && (
        <Dialog
          title={"Cancel connector request?"}
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

export default ConnectorEditRequest;
