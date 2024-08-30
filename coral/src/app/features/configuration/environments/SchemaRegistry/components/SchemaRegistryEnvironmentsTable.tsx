import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Link,
} from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { Environment } from "src/domain/environment";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

type SchemaRegistryEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
};

interface SchemaRegistryEnvironmentsTableRow {
  id: Environment["id"];
  type: Environment["type"];
  environmentName: Environment["name"];
  clusterName: Environment["clusterName"];
  tenantName: Environment["tenantName"];
  associatedEnv: Environment["associatedEnv"];
  status: Environment["envStatus"];
  envStatusTimeString: Environment["envStatusTimeString"];
}

const SchemaRegistryEnvironmentsTable = (
  props: SchemaRegistryEnvironmentsTableProps
) => {
  const { environments, ariaLabel } = props;

  const { isSuperAdminUser } = useAuthContext();

  const optionalColumnSuperAdmin: DataTableColumn<SchemaRegistryEnvironmentsTableRow> =
    {
      type: "custom",
      headerName: "Manage",
      UNSAFE_render: ({ type, id }: SchemaRegistryEnvironmentsTableRow) => {
        return (
          <Link href={`/modifyEnv?envId=${id}&envType=${type}`}>Edit</Link>
        );
      },
    };

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
      width: 450,
      UNSAFE_render: ({
        status,
        id,
        envStatusTimeString,
      }: SchemaRegistryEnvironmentsTableRow) => {
        return (
          <EnvironmentStatus
            envId={id}
            initialEnvStatus={status}
            initialUpdateTime={envStatusTimeString}
          />
        );
      },
    },
    ...(isSuperAdminUser ? [optionalColumnSuperAdmin] : []),
  ];

  const rows: SchemaRegistryEnvironmentsTableRow[] = environments.map((env) => {
    return {
      id: env.id,
      type: env.type,
      environmentName: env.name,
      clusterName: env.clusterName,
      tenantName: env.tenantName,
      associatedEnv: env.associatedEnv,
      status: env.envStatus,
      envStatusTimeString: env.envStatusTimeString,
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
