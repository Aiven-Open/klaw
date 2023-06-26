import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import {
  Navigate,
  useLocation,
  useMatches,
  useOutletContext,
} from "react-router-dom";
import { ConnectorOverviewResourcesTabs } from "src/app/features/connectors/details/components/ConnectorOverviewResourcesTabs";
import {
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  ConnectorOverviewTabEnum,
  isConnectorsOverviewTabEnum,
} from "src/app/router_utils";
import { ConnectorOverview } from "src/domain/connector";
import { getConnectorOverview } from "src/domain/connector/connector-api";

type ConnectorOverviewProps = {
  connectorName: string;
};

function findMatchingTab(
  matches: ReturnType<typeof useMatches>
): ConnectorOverviewTabEnum | undefined {
  const match = matches
    .map((match) => match.id)
    .find((id) =>
      Object.prototype.hasOwnProperty.call(
        CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
        id
      )
    );
  if (isConnectorsOverviewTabEnum(match)) {
    return match;
  }
  return undefined;
}

function ConnectorDetails(props: ConnectorOverviewProps) {
  const { connectorName } = props;
  const { state: initialEnvironment }: { state: string | null } = useLocation();

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  // @TODO add setEnvironmentId when getConnectorOverview is updated to take an environmentId param
  const [environmentId] = useState<string | undefined>(
    initialEnvironment ?? undefined
  );

  const {
    data: connectorData,
    isError: connectorIsError,
    error: connectorError,
    isLoading: connectorIsLoading,
    isFetched: connectorDataFetched,
  } = useQuery(["topic-overview", environmentId], {
    queryFn: () => getConnectorOverview({ connectornamesearch: connectorName }),
  });

  if (currentTab === undefined) {
    return (
      <Navigate to={`/connector/${connectorName}/overview`} replace={true} />
    );
  }

  return (
    <div>
      <h1>{connectorName}</h1>

      <ConnectorOverviewResourcesTabs
        isError={connectorIsError}
        error={connectorError}
        isLoading={connectorIsLoading}
        currentTab={currentTab}
        environmentId={environmentId}
        // We pass undefined when data is not fetched to avoid flash of stale data in the UI
        connectorOverview={connectorDataFetched ? connectorData : undefined}
      />
    </div>
  );
}

function useConnectorDetails() {
  return useOutletContext<{
    environmentId: string;
    connectorOverview: ConnectorOverview;
  }>();
}

export { ConnectorDetails, useConnectorDetails };
