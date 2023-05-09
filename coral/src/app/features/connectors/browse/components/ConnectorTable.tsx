import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Flexbox,
  StatusChip,
  InlineIcon,
} from "@aivenio/aquarium";
import { Connector } from "src/domain/connector";
import link from "@aivenio/aquarium/dist/src/icons/link";

type ConnectorTableProps = {
  connectors: Connector[];
  ariaLabel: string;
};

interface ConnectorTableRow {
  id: number;
  connectorName: Connector["connectorName"];
  environmentsList: Connector["environmentsList"];
  teamName: Connector["teamName"];
}

function ConnectorTable(props: ConnectorTableProps) {
  const { connectors, ariaLabel } = props;

  const columns: Array<DataTableColumn<ConnectorTableRow>> = [
    {
      type: "custom",
      field: "connectorName",
      headerName: "Connector",
      UNSAFE_render: ({ connectorName }: ConnectorTableRow) => (
        <a href={`/connectorOverview?connectorName=${connectorName}`}>
          {connectorName} <InlineIcon icon={link} />
        </a>
      ),
    },
    {
      type: "custom",
      field: "environmentsList",
      headerName: "Environments",
      UNSAFE_render: ({ environmentsList }: ConnectorTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {environmentsList?.map((env, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${env}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${env} `}
              />
            ))}
          </Flexbox>
        );
      },
    },
    {
      type: "text",
      field: "teamName",
      headerName: "Team",
    },
  ];

  const rows: ConnectorTableRow[] = connectors.map((connectors: Connector) => {
    return {
      id: Number(connectors.connectorId),
      connectorName: connectors.connectorName,
      teamName: connectors.teamName,
      environmentsList: connectors?.environmentsList ?? [],
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
