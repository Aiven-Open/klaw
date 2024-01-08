import { Box, DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { User } from "src/domain/user";
import uniqueId from "lodash/uniqueId";

type UsersProps = {
  users: User[];
  ariaLabel: string;
};

interface UsersRow {
  id: string;
  username: User["username"];
  fullname: User["fullname"];
  role: User["role"];
  team: User["team"];
  mailid: User["mailid"];
  switchTeams: User["switchTeams"];
  switchAllowedTeamNames: User["switchAllowedTeamNames"];
}

const UsersTable = ({ users, ariaLabel }: UsersProps) => {
  const columns: Array<DataTableColumn<UsersRow>> = [
    {
      type: "text",
      field: "username",
      headerName: "Username",
    },
    {
      type: "text",
      field: "fullname",
      headerName: "Name",
    },
    {
      type: "status",
      headerName: "Role",
      status: ({ role }) => ({
        status: role === "SUPERADMIN" ? "info" : "neutral",
        text: role,
      }),
    },
    {
      type: "text",
      field: "team",
      headerName: "Team",
    },
    {
      type: "text",
      field: "mailid",
      headerName: "Email ID",
    },
    {
      type: "status",
      headerName: "Switch teams",
      status: ({ switchTeams }) => ({
        status: switchTeams ? "success" : "neutral",
        text: switchTeams ? "Enabled" : "Disabled",
      }),
    },
    {
      type: "custom",
      width: 200,
      headerName: "Switch between teams",
      UNSAFE_render: ({ switchAllowedTeamNames }) => {
        if (switchAllowedTeamNames) {
          return (
            <Box.Flex
              component={"ul"}
              width={"fit"}
              flexWrap={"wrap"}
              gap={"1"}
            >
              {switchAllowedTeamNames.map((team, index) => (
                <li key={`${team}-${index}`}>
                  {team}
                  {index === switchAllowedTeamNames.length - 1 ? "" : ","}
                </li>
              ))}
            </Box.Flex>
          );
        }
      },
    },
  ];

  const rows: UsersRow[] = users.map((user) => {
    return {
      id: uniqueId(),
      username: user.username,
      fullname: user.fullname,
      role: user.role,
      team: user.team,
      mailid: user.mailid,
      switchTeams: user.switchTeams,
      switchAllowedTeamNames: user.switchAllowedTeamNames,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No users">
        There are no users data available.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export { UsersTable };
