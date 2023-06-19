import {
  Alert,
  BorderBox,
  Box,
  Button,
  Grid,
  Label,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Controller as _Controller } from "react-hook-form";
import { useNavigate } from "react-router-dom";
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
import { createConnectorRequest } from "src/domain/connector";
import {
  Environment,
  getAllEnvironmentsForConnector,
} from "src/domain/environment";
import { parseErrorMsg } from "src/services/mutation-utils";

function ConnectorRequest() {
  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const navigate = useNavigate();
  const toast = useToast();

  const form = useForm<ConnectorRequestFormSchema>({
    schema: connectorRequestFormSchema,
  });

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >({
    queryKey: ["getAllEnvironmentsForConnector"],
    queryFn: () => getAllEnvironmentsForConnector(),
  });

  const connectorRequestMutation = useMutation(createConnectorRequest, {
    onSuccess: () => {
      navigate("/requests/connectors?status=CREATED");
      toast({
        message: "Connector request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  function onSubmitForm(userInput: ConnectorRequestFormSchema) {
    connectorRequestMutation.mutate(userInput);
  }

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  return (
    <>
      <Box>
        {connectorRequestMutation.isError && (
          <Box marginBottom={"l1"} role="alert">
            <Alert type="error">
              {parseErrorMsg(connectorRequestMutation.error)}
            </Alert>
          </Box>
        )}
        <Form
          {...form}
          ariaLabel={"Request a new connector"}
          onSubmit={onSubmitForm}
        >
          {environmentsIsLoading && (
            <div data-testid={"environments-select-loading"}>
              <NativeSelect.Skeleton />
            </div>
          )}

          {environments && (
            <NativeSelect<ConnectorRequestFormSchema>
              name={"environment"}
              labelText={"Environment"}
              placeholder={"-- Please select --"}
              required
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

          <TextInput<ConnectorRequestFormSchema>
            name={"connectorName"}
            labelText={"Connector name"}
            required
          />

          <Label labelText="Connector configuration" required />
          <_Controller<ConnectorRequestFormSchema>
            name={"connectorConfig"}
            control={form.control}
            render={({ fieldState: { error } }) => {
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
            <SubmitButton disabled={connectorRequestMutation.isLoading}>
              Submit request
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

export default ConnectorRequest;
