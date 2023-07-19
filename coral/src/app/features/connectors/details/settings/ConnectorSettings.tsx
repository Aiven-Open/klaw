import {
  Alert,
  BorderBox,
  Box,
  Button,
  PageHeader,
  Skeleton,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorDeleteConfirmationModal } from "src/app/features/connectors/details/settings/components/ConnectorDeleteConfirmationModal";
import { deleteConnector } from "src/domain/connector";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

function ConnectorSettings() {
  const { environmentId, connectorOverview, connectorIsRefetching } =
    useConnectorDetails();

  const {
    connectorName,
    runningTasks,
    highestEnv,
    hasOpenRequest,
    connectorOwner,
  } = connectorOverview.connectorInfo;
  const showDeleteConnector =
    connectorOverview.connectorInfo.showDeleteConnector;

  const navigate = useNavigate();
  const toast = useToast();

  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>();

  const { mutate, isLoading } = useMutation(
    (remark?: string) =>
      deleteConnector({
        connectorName,
        envId: environmentId,
        remark,
      }),
    {
      onSuccess: () => {
        navigate("/connectors");
        toast({
          message: "Connector deletion request successfully created",
          position: "bottom-left",
          variant: "default",
        });
      },
      onError: (error: HTTPError) => {
        setErrorMessage(parseErrorMsg(error));
      },
      onSettled: () => {
        setShowConfirmation(false);
      },
    }
  );

  function getDeleteDisabledInformation() {
    const hasRunningTasks = runningTasks !== 0;

    return (
      <ul style={{ listStyle: "initial" }}>
        {hasRunningTasks && (
          <li>
            The connector has running tasks. Please wait until they are done
            before deleting the connector.
          </li>
        )}
        {!highestEnv && (
          <li>
            The connector is on a higher environment. Please delete the
            connector from that environment first.
          </li>
        )}
        {hasOpenRequest && <li>The connector has a pending request.</li>}
      </ul>
    );
  }

  return (
    <>
      {showConfirmation && (
        <ConnectorDeleteConfirmationModal
          isLoading={isLoading}
          onSubmit={(remark) => mutate(remark)}
          onClose={() => setShowConfirmation(false)}
        />
      )}

      <PageHeader title={"Settings"} />
      {errorMessage && (
        <Box role="alert" marginBottom={"l2"}>
          <Alert type="error">{errorMessage}</Alert>
        </Box>
      )}

      {!connectorOwner && (
        <div>
          Settings can only be edited by team members of the team the connector
          does belong to.
        </div>
      )}

      {connectorOwner && (
        <>
          <Typography.Subheading>Danger zone</Typography.Subheading>
          {connectorIsRefetching && (
            <div className={"visually-hidden"}>Loading information</div>
          )}
          {connectorIsRefetching && (
            <BorderBox
              date-testid={"connector-settings-danger-zone-content"}
              display={"flex"}
              flexDirection={"column"}
              borderColor={"error-60"}
              padding={"l2"}
              marginTop={"l2"}
              rowGap={"l2"}
            >
              <Box
                display={"flex"}
                alignItems={"center"}
                justifyContent={"space-between"}
              >
                <Box width={"full"}>
                  <Skeleton />
                  <Skeleton />
                </Box>
                <div>
                  {/* eslint-disable-next-line @typescript-eslint/no-empty-function */}
                  <Button.Primary onClick={() => {}} disabled={true}>
                    Request connector deletion
                  </Button.Primary>
                </div>
              </Box>
            </BorderBox>
          )}

          {!connectorIsRefetching && (
            <BorderBox
              date-testid={"connector-settings-danger-zone-content"}
              display={"flex"}
              flexDirection={"column"}
              borderColor={"error-60"}
              padding={"l2"}
              marginTop={"l2"}
              rowGap={"l2"}
              aria-hidden={connectorIsRefetching}
            >
              {!showDeleteConnector && (
                <Alert type={"warning"}>
                  {connectorIsRefetching ? (
                    <Typography.DefaultStrong htmlTag={"div"}>
                      <Skeleton />
                    </Typography.DefaultStrong>
                  ) : (
                    <>
                      You can not create a delete request for this connector:{" "}
                      <br />
                      {getDeleteDisabledInformation()}
                    </>
                  )}
                </Alert>
              )}
              <Box
                display={"flex"}
                alignItems={"center"}
                justifyContent={"space-between"}
              >
                <div>
                  <Typography.DefaultStrong htmlTag={"h3"}>
                    Request connector deletion
                  </Typography.DefaultStrong>
                  <Box component={"p"}>
                    Once a request for deletion is approved, there is no going
                    back. Please be certain.
                  </Box>
                </div>

                <div>
                  <Button.Primary
                    onClick={() => setShowConfirmation(true)}
                    disabled={!showDeleteConnector}
                  >
                    Request connector deletion
                  </Button.Primary>
                </div>
              </Box>
            </BorderBox>
          )}
        </>
      )}
    </>
  );
}

export { ConnectorSettings };
