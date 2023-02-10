import { DataTable } from "@aivenio/aquarium";
import { CreatedSchemaRequests } from "src/domain/schema-request/schema-request-types";
import {
  columns,
  SchemaRequestTableData,
} from "src/app/features/approvals/schemas/components/schema-request-table";

type SchemaApprovalsTableProps = {
  requests: CreatedSchemaRequests[];
};
function SchemaApprovalsTable(props: SchemaApprovalsTableProps) {
  const { requests } = props;

  const rows: SchemaRequestTableData[] = requests.map(
    (request: CreatedSchemaRequests) => {
      return {
        id: request.req_no,
        topicname: request.topicname,
        environmentName: request.environmentName,
        username: request.username,
        requesttimestring: request.requesttimestring,
      };
    }
  );

  return (
    <DataTable
      ariaLabel={"Schema requests"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export default SchemaApprovalsTable;
