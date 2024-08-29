import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Link,
} from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { Environment } from "src/domain/environment";

type KafkaConnectEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
  isSuperAdminUser?: boolean;
};

interface KafkaConnectEnvironmentsTableRow {
  id: Environment["id"];
  type: Environment["type"];
  environmentName: Environment["name"];
  clusterName: Environment["clusterName"];
  tenantName: Environment["tenantName"];
  status: Environment["envStatus"];
  envStatusTimeString: Environment["envStatusTimeString"];
}

const KafkaConnectEnvironmentsTable = (
  props: KafkaConnectEnvironmentsTableProps
) => {
  const { environments, ariaLabel } = props;
  const isSuperAdminUser =
    props.isSuperAdminUser !== undefined ? props.isSuperAdminUser : false;

  const optionalColumnSuperAdmin: DataTableColumn<KafkaConnectEnvironmentsTableRow> =
    {
      type: "custom",
      headerName: "Manage",
      UNSAFE_render: ({ type, id }: KafkaConnectEnvironmentsTableRow) => {
        return (
          <Link href={`/modifyEnv?envId=${id}&envType=${type}`}>Edit</Link>
        );
      },
    };

  const columns: Array<DataTableColumn<KafkaConnectEnvironmentsTableRow>> = [
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
      headerName: "Status",
      width: 450,
      UNSAFE_render: ({
        status,
        id,
        envStatusTimeString,
      }: KafkaConnectEnvironmentsTableRow) => {
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

  const rows: KafkaConnectEnvironmentsTableRow[] = environments.map((env) => {
    return {
      id: env.id,
      type: env.type,
      environmentName: env.name,
      clusterName: env.clusterName,
      tenantName: env.tenantName,
      status: env.envStatus,
      envStatusTimeString: env.envStatusTimeString,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Kafka Connect Environments">
        No Kafka Connect Environment matched your criteria.
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

export default KafkaConnectEnvironmentsTable;
