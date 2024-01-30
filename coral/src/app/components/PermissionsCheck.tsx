import { useAuthContext } from "src/app/context-provider/AuthProvider";

type Permission =
  | "canShutdownKw"
  | "canUpdatePermissions"
  | "addEditRoles"
  | "viewTopics"
  | "requestItems"
  | "viewKafkaConnect"
  | "syncBackTopics"
  | "syncBackSchemas"
  | "syncBackAcls"
  | "updateServerConfig"
  | "showServerConfigEnvProperties"
  | "addUser"
  | "addTeams"
  | "syncTopicsAcls"
  | "syncConnectors"
  | "manageConnectors"
  | "syncSchemas"
  | "approveAtleastOneRequest"
  | "approveDeclineTopics"
  | "approveDeclineOperationalReqs"
  | "approveDeclineSubscriptions"
  | "approveDeclineSchemas"
  | "approveDeclineConnectors"
  | "showAddDeleteTenants"
  | "addDeleteEditClusters"
  | "addDeleteEditEnvs";

const PermissionsCheck = ({
  permission,
  children,
  placeholder,
}: {
  permission: Permission;
  children: React.ReactNode;
  placeholder?: React.ReactNode;
}) => {
  const { permissions } = useAuthContext();
  return permissions[permission] ? children : placeholder ? placeholder : null;
};

export default PermissionsCheck;
