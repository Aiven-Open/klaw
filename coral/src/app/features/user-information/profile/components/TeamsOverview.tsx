import {
  Box,
  Checkbox,
  DataTable,
  DataTableColumn,
  EmptyState,
  Typography,
} from "@aivenio/aquarium";
import uniqueId from "lodash/uniqueId";

type TeamsOverviewProps = {
  teams: string[];
};

interface TeamsOverviewRow {
  id: string;
  teamName: string;
}

const TeamsOverview = ({ teams }: TeamsOverviewProps) => {
  const columns: Array<DataTableColumn<TeamsOverviewRow>> = [
    {
      type: "text",
      field: "teamName",
      headerName: "Team name",
    },
  ];

  const rows: TeamsOverviewRow[] = teams.map((team) => {
    return {
      id: uniqueId(),
      teamName: team,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No teams">
        There are no team data available.
      </EmptyState>
    );
  }

  return (
    <Box.Flex
      flexDirection={"column"}
      rowGap={"l2"}
      paddingBottom={"l2"}
      paddingTop={"l4"}
    >
      <Typography.Large htmlTag={"h2"}>Teams</Typography.Large>
      <Checkbox
        checked={true}
        disabled={true}
        caption={
          "Enable team switching for users with multiple teams in Klaw interface."
        }
      >
        Switch between Teams
      </Checkbox>

      <DataTable
        ariaLabel={"Teams user belongs to"}
        columns={columns}
        rows={rows}
        noWrap={false}
      />
    </Box.Flex>
  );
};

export { TeamsOverview };
