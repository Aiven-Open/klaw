import {
  AuthUser,
  AuthUserLoginData,
} from "src/domain/auth-user/auth-user-types";

import api, { API_PATHS } from "src/services/api";
import { paths as ApiPaths } from "types/api";
import { KlawApiResponse } from "types/utils";

const getAuthUser = (userLogin: AuthUserLoginData): Promise<AuthUser> => {
  const data = new URLSearchParams();
  data.append("username", userLogin.username);
  data.append("password", userLogin.password);

  // /login is a path which does not currently exist, because there is no auth flow in coral currently
  // getAuthUser is used in a component that is currently never rendered (LoginForm)
  // We coerce the keyof ApiPaths to avoid TS compiling error
  return api.post("/login" as keyof ApiPaths, data);
};

function getUserTeamName(): Promise<string> {
  return api
    .get<KlawApiResponse<"getAuth">>(API_PATHS.getAuth)
    .then((response) => response.teamname);
}

export { getAuthUser, getUserTeamName };
