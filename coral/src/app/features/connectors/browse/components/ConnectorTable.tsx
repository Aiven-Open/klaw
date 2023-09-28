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
import { createConnectorOverviewLink } from "src/app/features/topics/browse/utils/create-topic-overview-link";
import { Connector } from "src/domain/connector";
import useFeatureFlag from "src/services/feature-flags/hook/useFeatureFlag";
import { FeatureFlag } from "src/services/feature-flags/types";

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
  const connectorDetailsEnabled = useFeatureFlag(
    FeatureFlag.FEATURE_FLAG_CONNECTOR_OVERVIEW
  );

  const columns: Array<DataTableColumn<ConnectorTableRow>> = [
    {
      type: "custom",
      headerName: "Connector",
      UNSAFE_render: ({ connectorName, environmentId }: ConnectorTableRow) => {
        if (!connectorDetailsEnabled) {
          return (
            <a href={createConnectorOverviewLink(connectorName)}>
              {connectorName} <InlineIcon icon={link} />
            </a>
          );
        }
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
          <Box flexWrap={"wrap"} gap={"2"} component={"ul"}>
            {environmentsList?.map(({ name, id }) => (
              <li key={id}>
                <StatusChip dense status="neutral" text={name} />
              </li>
            ))}
          </Box>
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
