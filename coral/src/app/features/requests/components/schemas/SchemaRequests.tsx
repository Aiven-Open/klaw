import { useQuery } from "@tanstack/react-query";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequestTable } from "src/app/features/requests/components/schemas/components/SchemaRequestTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";

function SchemaRequests() {
  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["schemaRequests"],
    queryFn: () => getSchemaRequests({ pageNo: String(1) }),
  });

  return (
    <TableLayout
      filters={[]}
      table={<SchemaRequestTable requests={schemaRequests?.entries || []} />}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { SchemaRequests };
