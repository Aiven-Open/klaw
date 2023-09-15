import { Alert, Box, Icon, Tabs } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
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

  const tabsMap: Array<{
    connectorOverviewTabEnum: ConnectorOverviewTabEnum;
    title: string;
  }> = [
    {
      connectorOverviewTabEnum: ConnectorOverviewTabEnum.OVERVIEW,
      title: "Overview",
    },
    {
      connectorOverviewTabEnum: ConnectorOverviewTabEnum.DOCUMENTATION,
      title: "Readme",
    },
    {
      connectorOverviewTabEnum: ConnectorOverviewTabEnum.HISTORY,
      title: "History",
    },
    {
      connectorOverviewTabEnum: ConnectorOverviewTabEnum.SETTINGS,
      title: "Settings",
    },
  ];

  function renderTabContent() {
    if (isError) {
      return (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="error">
            There was an error trying to load the connector details:{" "}
            {parseErrorMsg(error)}.
            <br />
            Please try again later.
          </Alert>
        </Box>
      );
    }

    if (isLoading || connectorIsRefetching) {
      return (
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading connector details</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      );
    }

    if (!connectorOverview?.connectorExists) {
      return (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="warning">
            Connector {connectorName} does not exist.
          </Alert>
        </Box>
      );
    }

    return (
      <div data-testid={"tabpanel-content"}>
        <Outlet
          context={{
            environmentId:
              environmentId || connectorOverview?.connectorInfo.environmentId,
            connectorOverview,
            connectorIsRefetching,
          }}
        />
      </div>
    );
  }

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      {tabsMap.map((tab) => {
        return (
          <Tabs.Tab
            title={tab.title}
            value={tab.connectorOverviewTabEnum}
            aria-label={tab.title}
            key={tab.title}
          >
            {currentTab === tab.connectorOverviewTabEnum && (
              <div>
                <PreviewBanner
                  linkTarget={`/connectorOverview?connectorName=${connectorName}`}
                />
                {renderTabContent()}
              </div>
            )}
          </Tabs.Tab>
        );
      })}
    </Tabs>
  );
}

export { ConnectorOverviewResourcesTabs };
