import { KlawApiModel } from "types/utils";

type AuthUser = {
  username: KlawApiModel<"AuthenticationInfo">["username"];
  userrole: KlawApiModel<"AuthenticationInfo">["userrole"];
  teamname: KlawApiModel<"AuthenticationInfo">["teamname"];
  teamId: KlawApiModel<"AuthenticationInfo">["teamId"];
  canSwitchTeams: KlawApiModel<"AuthenticationInfo">["canSwitchTeams"];
};

type AuthUserLoginData = {
  username: string;
  password: string;
};

export type { AuthUser, AuthUserLoginData };
