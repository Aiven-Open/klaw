import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { ConnectorRequestsTable } from "src/app/features/requests/connectors/components/ConnectorRequestsTable";
import { getConnectorRequests } from "src/domain/connector";

function ConnectorRequests() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["connectorRequests"],
    queryFn: () =>
      getConnectorRequests({
        pageNo: "1",
      }),
  });

  return (
    <TableLayout
      filters={[]}
      table={
        <ConnectorRequestsTable
          ariaLabel="Connector requests"
          requests={data?.entries ?? []}
          onDetails={() => null}
          onDelete={() => null}
        />
      }
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { ConnectorRequests };
