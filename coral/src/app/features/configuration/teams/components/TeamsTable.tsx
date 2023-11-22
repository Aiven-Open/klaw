import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { Team } from "src/domain/team";

type TeamsTableProps = {
  teams: Team[];
};

interface TeamsTableRow {
  id: Team["teamId"];
  teamname: Team["teamname"];
  teammail: Team["teammail"];
  teamphone: Team["teamphone"];
  contactperson: Team["contactperson"];
  tenantName: Team["tenantName"];
}

const TeamsTable = ({ teams }: TeamsTableProps) => {
  const columns: Array<DataTableColumn<TeamsTableRow>> = [
    {
      type: "text",
      field: "teamname",
      headerName: "Team name",
    },
    {
      type: "text",
      field: "teammail",
      headerName: "Team email",
    },
    {
      type: "text",
      field: "teamphone",
      headerName: "Team phone",
    },
    {
      type: "text",
      field: "contactperson",
      headerName: "Contact person",
    },
    {
      type: "text",
      field: "tenantName",
      headerName: "Tenant",
    },
  ];

  const rows: TeamsTableRow[] = teams.map((team) => {
    return {
      id: team.teamId,
      teamname: team.teamname,
      teammail: team.teammail,
      teamphone: team.teamphone,
      contactperson: team.contactperson,
      tenantName: team.tenantName,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No teams">
        There are no teams data available.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={"Teams overview"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export { TeamsTable };
