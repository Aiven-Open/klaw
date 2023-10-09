import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
  StatusChip,
} from "@aivenio/aquarium";
import link from "@aivenio/aquarium/dist/src/icons/link";
import { Link } from "react-router-dom";
import { Connector } from "src/domain/connector";

type ConnectorTableProps = {
  connectors: Connector[];
  ariaLabel: string;
};

interface ConnectorTableRow {
  id: number;
  connectorName: Connector["connectorName"];
  environmentsList: Connector["environmentsList"];
  teamName: Connector["teamName"];
  environmentId: Connector["environmentId"];
}

function ConnectorTable(props: ConnectorTableProps) {
  const { connectors, ariaLabel } = props;

  const columns: Array<DataTableColumn<ConnectorTableRow>> = [
    {
      type: "custom",
      headerName: "Connector",
      UNSAFE_render: ({ connectorName, environmentId }: ConnectorTableRow) => {
        return (
          <Link
            to={`/connector/${connectorName}/overview`}
            state={environmentId}
          >
            {connectorName} <InlineIcon icon={link} />
          </Link>
        );
      },
    },
    {
      type: "custom",
      headerName: "Environments",
      UNSAFE_render: ({ environmentsList }: ConnectorTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {environmentsList?.map(({ name, id }) => (
              <li key={id}>
                <StatusChip dense status="neutral" text={name} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "text",
      field: "teamName",
      headerName: "Team",
    },
  ];

  const rows: ConnectorTableRow[] = connectors.map((connector: Connector) => {
    return {
      id: Number(connector.connectorId),
      connectorName: connector.connectorName,
      teamName: connector.teamName,
      environmentsList: connector?.environmentsList ?? [],
      environmentId: connector.environmentId,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Connectors">
        No Connectors matched your criteria.
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
}
export default ConnectorTable;
