import {
  AuthUser,
  AuthUserLoginData,
} from "src/domain/auth-user/auth-user-types";
import api, { API_PATHS } from "src/services/api";
import { paths as ApiPaths } from "types/api";
import { KlawApiResponse } from "types/utils";

const getAuthUserMockForLogin = (
  userLogin: AuthUserLoginData
): Promise<AuthUser> => {
  // /login is a path which does not currently exist, because there is no auth flow in coral currently
  // getAuthUser is used in a component that is currently never rendered (LoginForm)
  // We coerce the keyof ApiPaths to avoid TS compiling error
  return api.post("/login" as keyof ApiPaths, userLogin);
};

function transformAuthResponse(response: KlawApiResponse<"getAuth">): AuthUser {
  return {
    username: response.username,
    teamname: response.teamname,
    teamId: response.teamId,
    canSwitchTeams: response.canSwitchTeams,
    userrole: response.userrole,
  };
}

function getAuth(): Promise<AuthUser> {
  return api
    .get<KlawApiResponse<"getAuth">>(API_PATHS.getAuth)
    .then((response) => transformAuthResponse(response));
}

export { getAuthUserMockForLogin, getAuth };
