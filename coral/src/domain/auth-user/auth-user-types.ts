import { KlawApiModel } from "types/utils";

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

type AuthUser = {
  username: KlawApiModel<"AuthenticationInfo">["username"];
  userrole: KlawApiModel<"AuthenticationInfo">["userrole"];
  teamname: KlawApiModel<"AuthenticationInfo">["teamname"];
  teamId: KlawApiModel<"AuthenticationInfo">["teamId"];
  canSwitchTeams: KlawApiModel<"AuthenticationInfo">["canSwitchTeams"];
  totalTeamTopics: number;
  totalOrgTopics: number;
  permissions: Record<Permission, boolean>;
};

export type { AuthUser, Permission };
