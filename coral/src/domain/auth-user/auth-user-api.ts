import { AuthUser } from "src/domain/auth-user/auth-user-types";
import api, { API_PATHS, HTTPError } from "src/services/api";
import { KlawApiResponse } from "types/utils";

// user roles are added dynamically by admins, we can't make them enums
// but "SUPERADMIN" is the default role set by us. It's highly unlikely
// that users would change that string, so we can use it to identify
// the superadmin role, which has different view-access
const SUPERADMIN_USERROLE = "SUPERADMIN";
type SuperAdminUser = AuthUser & { userrole: typeof SUPERADMIN_USERROLE };
function isSuperAdmin(user: AuthUser): user is SuperAdminUser {
  return user.userrole === SUPERADMIN_USERROLE;
}

function transformAuthResponse(response: KlawApiResponse<"getAuth">): AuthUser {
  const headers = {} as Headers;

  if (isSuperAdmin(response)) {
    // We're marking the SUPERADMIN as not authorized
    // by manually throwing the correct error that will
    // be caught by isUnauthorizedError
    const error: HTTPError = {
      data: " ",
      headers,
      status: 401,
      statusText: "Not authorized to access Coral.",
    };
    throw error;
  }

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

function logoutUser() {
  return api.post<KlawApiResponse<"logout">, never>(API_PATHS.logout);
}

export { getAuth, logoutUser };
