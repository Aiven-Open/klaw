import { Alert, Box, Icon, Tabs } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  ConnectorOverviewTabEnum,
  isConnectorsOverviewTabEnum,
} from "src/app/router_utils";
import { ConnectorOverview } from "src/domain/connector";
import { parseErrorMsg } from "src/services/mutation-utils";

type Props = {
  currentTab: ConnectorOverviewTabEnum;
  environmentId?: string;
  error?: unknown;
  isError: boolean;
  isLoading: boolean;
  connectorIsRefetching: boolean;
  connectorOverview?: ConnectorOverview;
  connectorName: string;
};

function ConnectorOverviewResourcesTabs({
  currentTab,
  environmentId,
  error,
  isError,
  isLoading,
  connectorOverview,
  connectorIsRefetching,
  connectorName,
}: Props) {
  const navigate = useNavigate();
  function navigateToTab(
    navigate: NavigateFunction,
    resourceTypeId: unknown
  ): void {
    if (isConnectorsOverviewTabEnum(resourceTypeId)) {
      navigate(
        `/connector/${connectorName}/${CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH[resourceTypeId]}`,
        {
          replace: true,
        }
      );
    }
  }

  const tabContent = (
    <div>
      {isError && (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="error">
            There was an error trying to load the connector details:{" "}
            {parseErrorMsg(error)}.
            <br />
            Please try again later.
          </Alert>
        </Box>
      )}

      {(isLoading || connectorIsRefetching) && (
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading connector details</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      )}

      {!isLoading && !connectorOverview?.connectorExists && (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="warning">
            Connector {connectorName} does not exist.
          </Alert>
        </Box>
      )}

      {!isError &&
        !isLoading &&
        !connectorIsRefetching &&
        connectorOverview?.connectorExists && (
          <div data-testid={"tabpanel-content"}>
            <Outlet
              context={{
                environmentId:
                  environmentId ||
                  connectorOverview?.connectorInfo.environmentId,
                connectorOverview,
                connectorIsRefetching,
              }}
            />
          </div>
        )}
    </div>
  );

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title={"Overview"}
        value={ConnectorOverviewTabEnum.OVERVIEW}
        aria-label={"Overview"}
        key={"Overview"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Readme"}
        value={ConnectorOverviewTabEnum.DOCUMENTATION}
        aria-label={"Readme"}
        key={"Readme"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"History"}
        value={ConnectorOverviewTabEnum.HISTORY}
        aria-label={"History"}
        key={"History"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Settings"}
        value={ConnectorOverviewTabEnum.SETTINGS}
        aria-label={"Settings"}
        key={"Settings"}
      >
        {tabContent}
      </Tabs.Tab>
    </Tabs>
  );
}

export { ConnectorOverviewResourcesTabs };
