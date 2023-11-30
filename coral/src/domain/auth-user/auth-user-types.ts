import { KlawApiModel } from "types/utils";

type AuthUser = {
  username: KlawApiModel<"AuthenticationInfo">["username"];
  userrole: KlawApiModel<"AuthenticationInfo">["userrole"];
  teamname: KlawApiModel<"AuthenticationInfo">["teamname"];
  teamId: KlawApiModel<"AuthenticationInfo">["teamId"];
  canSwitchTeams: KlawApiModel<"AuthenticationInfo">["canSwitchTeams"];
  pendingTopicRequests: number;
  pendingAclRequests: number;
  pendingSchemaRequests: number;
  pendingConnectorRequests: number;
  totalPendingRequests: number;
};

export type { AuthUser };
