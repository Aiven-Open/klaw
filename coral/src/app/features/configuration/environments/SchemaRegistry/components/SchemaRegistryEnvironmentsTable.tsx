import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { Environment } from "src/domain/environment";

type SchemaRegistryEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
};

interface SchemaRegistryEnvironmentsTableRow {
  id: Environment["id"];
  environmentName: Environment["name"];
  clusterName: Environment["clusterName"];
  tenantName: Environment["tenantName"];
  associatedEnv: Environment["associatedEnv"];
  status: Environment["envStatus"];
}

const SchemaRegistryEnvironmentsTable = (
  props: SchemaRegistryEnvironmentsTableProps
) => {
  const { environments, ariaLabel } = props;

  const columns: Array<DataTableColumn<SchemaRegistryEnvironmentsTableRow>> = [
    {
      type: "text",
      field: "environmentName",
      headerName: "Environment",
    },
    {
      type: "text",
      field: "clusterName",
      headerName: "Cluster",
    },
    {
      type: "text",
      field: "tenantName",
      headerName: "Tenant",
    },
    {
      type: "custom",
      headerName: "Associated Kafka Environment",
      UNSAFE_render: ({
        associatedEnv,
      }: SchemaRegistryEnvironmentsTableRow) => {
        if (associatedEnv === undefined) {
          return "-";
        }

        return associatedEnv.name;
      },
    },
    {
      type: "custom",
      headerName: "Status",
      width: 150,
      UNSAFE_render: ({ status, id }: SchemaRegistryEnvironmentsTableRow) => {
        return <EnvironmentStatus envId={id} initialEnvStatus={status} />;
      },
    },
  ];

  const rows: SchemaRegistryEnvironmentsTableRow[] = environments.map((env) => {
    return {
      id: env.id,
      environmentName: env.name,
      clusterName: env.clusterName,
      tenantName: env.tenantName,
      associatedEnv: env.associatedEnv,
      status: env.envStatus,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Schema Registry Environments">
        No Schema Registry Environment matched your criteria.
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

export default SchemaRegistryEnvironmentsTable;
