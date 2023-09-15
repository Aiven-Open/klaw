import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useEffect, useState } from "react";
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
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Environment,
  getAllEnvironmentsForConnector,
} from "src/domain/environment";
import {
  Form,
  NativeSelect,
  SubmitButton,
  Textarea,
  TextInput,
  useForm,
} from "src/app/components/Form";
import {
  ConnectorRequestFormSchema,
  connectorRequestFormSchema,
} from "src/app/features/connectors/request/schemas/connector-request-form";
import { Controller as _Controller, SubmitHandler } from "react-hook-form";
import MonacoEditor from "@monaco-editor/react";
import {
  ConnectorDetailsForEnv,
  getConnectorDetailsPerEnv,
  requestConnectorPromotion,
} from "src/domain/connector";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { ConnectorSkeletonForm } from "src/app/features/connectors/request/components/ConnectorSkeletonForm";
import { Dialog } from "src/app/components/Dialog";
import isEmpty from "lodash/isEmpty";

function ConnectorPromotionRequest() {
  const [targetEnv, setTargetEnv] = useState<Environment | undefined>(
    undefined
  );

  const [sourceEnv, setSourceEnv] = useState<Environment | undefined>(
    undefined
  );

  const [cancelDialogVisible, setCancelDialogVisible] = useState(false);

  const { connectorName } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();

  const sourceEnvId = searchParams.get("sourceEnv");
  const targetEnvId = searchParams.get("targetEnv");

  function showErrorToast(message: string) {
    toast({
      message: message,
      position: "bottom-left",
      variant: "danger",
    });
  }

  // redirect user in case source or target env id are not set
  useEffect(() => {
    if (!targetEnvId || !sourceEnvId) {
      showErrorToast(`Missing url parameter`);
      navigate(`/connector/${connectorName}`, { replace: true });
      return;
    }
  }, [sourceEnvId, targetEnvId]);

  const {
    data: environments,
    isFetched: isFetchedEnvironments,
    isError: isErrorEnvironments,
    error: errorEnvironments,
  } = useQuery<Environment[], Error>({
    queryKey: ["getAllEnvironmentsForConnector"],
    queryFn: () => getAllEnvironmentsForConnector(),
  });

  useEffect(() => {
    if (isFetchedEnvironments) {
      // redirect user back in case there is an error
      if (isErrorEnvironments) {
        showErrorToast(
          `Error while fetching available environments: ${parseErrorMsg(
            errorEnvironments
          )}`
        );
        navigate(`/connector/${connectorName}`, { replace: true });
        return;
      }

      // redirect user back in case source or target env do not exist
      // e.g. because user came from old url
      const sourceEnvFromUrl = environments?.find((env) => {
        return env.id === sourceEnvId;
      });

      const targetEnvFromUrl = environments?.find((env) => {
        return env.id === targetEnvId;
      });

      if (!sourceEnvFromUrl) {
        showErrorToast(
          `No source environment was found with ID ${sourceEnvId}`
        );
        navigate(`/connector/${connectorName}`, { replace: true });
        return;
      }

      if (!targetEnvFromUrl) {
        showErrorToast(
          `No target environment was found with ID ${targetEnvId}`
        );
        navigate(`/connector/${connectorName}`, { replace: true });
        return;
      }

      setTargetEnv(targetEnvFromUrl);
      setSourceEnv(sourceEnvFromUrl);
    }
  }, [isFetchedEnvironments, isErrorEnvironments]);

  const {
    data: connectorDetails,
    isFetched: isFetchedConnectorDetails,
    isError: isErrorConnectorDetails,
    error: errorConnectorDetails,
  } = useQuery<ConnectorDetailsForEnv, HTTPError>(
    ["getConnectorDetailsPerEnv", connectorName],
    {
      queryFn: () =>
        getConnectorDetailsPerEnv({
          connectorName: connectorName || "",
          envSelected: sourceEnvId || "",
        }),
      enabled: sourceEnv !== null,
    }
  );

  useEffect(() => {
    if (isFetchedConnectorDetails) {
      // redirect user back in case there is an error
      if (isErrorConnectorDetails) {
        showErrorToast(
          `Error while fetching connector ${connectorName}: ${parseErrorMsg(
            errorConnectorDetails
          )}`
        );
        navigate(`/connector`, { replace: true });
        return;
      }

      // redirect user back in case the connector does not exists
      // e.g. in case they followed an old url
      if (!connectorDetails?.connectorExists) {
        showErrorToast(`No connector was found with name ${connectorName}`);
        navigate(`/connector`, { replace: true });
        return;
      }
    }
  }, [isFetchedConnectorDetails]);

  const {
    mutate: requestPromotion,
    isLoading: isLoadingRequestPromotion,
    isError: isErrorRequestPromotion,
    error: errorRequestPromotion,
  } = useMutation(requestConnectorPromotion, {
    onSuccess: () => {
      navigate(-1);
      toast({
        message: "Connector update request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
  });

  const form = useForm<ConnectorRequestFormSchema>({
    schema: connectorRequestFormSchema,
    values: {
      connectorConfig:
        connectorDetails?.connectorContents?.connectorConfig || "",
      connectorName: connectorName || "",
      environment: targetEnv?.id || "",
      description: connectorDetails?.connectorContents?.description || "",
    },
  });

  const onSubmit: SubmitHandler<ConnectorRequestFormSchema> = (data) => {
    requestPromotion(data);
  };

  function cancelRequest() {
    form.reset();
    navigate(-1);
  }

  if (!isFetchedConnectorDetails || !isFetchedEnvironments) {
    return <ConnectorSkeletonForm />;
  }

  return (
    <>
      <Box>
        {isErrorRequestPromotion && (
          <Box marginBottom={"l1"}>
            <Alert type="error">{parseErrorMsg(errorRequestPromotion)}</Alert>
          </Box>
        )}

        <Form
          {...form}
          ariaLabel={"Request connector promotion"}
          onSubmit={onSubmit}
        >
          {targetEnv && (
            <NativeSelect<ConnectorRequestFormSchema>
              name="environment"
              labelText={"Environment (read-only)"}
              readOnly
            >
              <Option key={targetEnv.id} value={targetEnv.id}>
                {targetEnv.name}
              </Option>
            </NativeSelect>
          )}

          <TextInput<ConnectorRequestFormSchema>
            name={"connectorName"}
            value={connectorName}
            labelText={"Connector name (read-only)"}
            readOnly
          />

          <div aria-hidden={"true"}>
            <Label labelText="Connector configuration" required />
          </div>
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
                      data-testid="connector-config"
                      height="200px"
                      language="json"
                      theme={"light"}
                      onChange={(value) => {
                        value !== undefined &&
                          form.setValue("connectorConfig", value, {
                            shouldValidate: true,
                          });
                      }}
                      options={{
                        language: "json",
                        ariaLabel: "Connector configuration, required",
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
              value={connectorDetails?.connectorContents?.description}
              labelText={"Connector description (read-only)"}
              readOnly={true}
            />
            <Textarea<ConnectorRequestFormSchema>
              name={"remarks"}
              labelText={"Message for approval"}
            />
          </Grid>

          <Box display={"flex"} colGap={"l1"} marginTop={"3"}>
            <SubmitButton loading={isLoadingRequestPromotion}>
              Submit promotion request
            </SubmitButton>
            <Button
              type="button"
              kind={"secondary"}
              disabled={isLoadingRequestPromotion}
              onClick={() => {
                if (!isEmpty(form.formState.touchedFields)) {
                  setCancelDialogVisible(true);
                  return;
                }
                cancelRequest();
              }}
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
            onClick: () => {
              form.reset();
              navigate(-1);
            },
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

export { ConnectorPromotionRequest };
