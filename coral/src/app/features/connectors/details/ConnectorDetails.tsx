import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  Navigate,
  useLocation,
  useMatches,
  useOutletContext,
} from "react-router-dom";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";
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

  const [environmentId, setEnvironmentId] = useState<string | undefined>(
    initialEnvironment ?? undefined
  );

  const {
    data: connectorData,
    isError: connectorIsError,
    error: connectorError,
    isLoading: connectorIsLoading,
    isRefetching: connectorIsRefetching,
  } = useQuery(["connector-overview", connectorName, environmentId], {
    queryFn: () =>
      getConnectorOverview({
        connectornamesearch: connectorName,
        environmentId,
      }),
  });

  useEffect(() => {
    if (
      connectorData?.availableEnvironments !== undefined &&
      environmentId === undefined
    ) {
      setEnvironmentId(connectorData?.availableEnvironments[0].id);
    }
  }, [connectorData?.availableEnvironments, environmentId, setEnvironmentId]);

  if (currentTab === undefined) {
    return (
      <Navigate to={`/connector/${connectorName}/overview`} replace={true} />
    );
  }

  return (
    <div>
      <EntityDetailsHeader
        entity={{ name: connectorName, type: "connector" }}
        entityExists={Boolean(connectorData?.connectorExists)}
        entityUpdating={connectorIsRefetching}
        entityEditLink={
          "/connector/connectorName=SchemaTest&env=${connectorOverview.availableEnvironments[0].id}&requestType=edit"
        }
        environments={connectorData?.availableEnvironments}
        environmentId={environmentId}
        setEnvironmentId={setEnvironmentId}
        showEditButton={Boolean(connectorData?.connectorInfo.showEditConnector)}
      />

      <ConnectorOverviewResourcesTabs
        isError={connectorIsError}
        error={connectorError}
        isLoading={connectorIsLoading}
        currentTab={currentTab}
        environmentId={environmentId}
        connectorOverview={connectorData}
        connectorIsRefetching={connectorIsRefetching}
      />
    </div>
  );
}

function useConnectorDetails() {
  return useOutletContext<{
    environmentId: string;
    connectorOverview: ConnectorOverview;
    connectorIsRefetching: boolean;
  }>();
}

export { ConnectorDetails, useConnectorDetails };
