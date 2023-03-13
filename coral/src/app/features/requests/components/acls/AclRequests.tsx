import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getAclRequests } from "src/domain/acl/acl-api";
import { AclRequestsTable } from "src/app/features/requests/components/acls/components/AclRequestsTable";

function AclRequests() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["aclRequests"],
    queryFn: () =>
      getAclRequests({
        pageNo: "1",
      }),
  });

  return (
    <TableLayout
      filters={[]}
      table={
        <AclRequestsTable
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

export { AclRequests };
