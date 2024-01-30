import { AuthUser } from "src/domain/auth-user/auth-user-types";
import api, { API_PATHS } from "src/services/api";
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
  const {
    username,
    teamname,
    teamId,
    canSwitchTeams,
    userrole,
    myteamtopics,
    myOrgTopics,
    canShutdownKw,
    canUpdatePermissions,
    addEditRoles,
    viewTopics,
    // requestItems,
    viewKafkaConnect,
    syncBackTopics,
    syncBackSchemas,
    syncBackAcls,
    updateServerConfig,
    showServerConfigEnvProperties,
    addUser,
    addTeams,
    syncTopicsAcls,
    syncConnectors,
    // // manageConnectors,
    syncSchemas,
    approveAtleastOneRequest,
    // approveDeclineTopics,
    approveDeclineOperationalReqs,
    approveDeclineSubscriptions,
    approveDeclineSchemas,
    approveDeclineConnectors,
    showAddDeleteTenants,
    addDeleteEditClusters,
    addDeleteEditEnvs,
  } = response;

  return {
    username,
    teamname,
    teamId,
    canSwitchTeams,
    userrole,
    totalTeamTopics: parseInt(myteamtopics),
    totalOrgTopics: parseInt(myOrgTopics),
    permissions: {
      canShutdownKw: canShutdownKw === "Authorized",
      canUpdatePermissions: canUpdatePermissions === "Authorized",
      addEditRoles: addEditRoles === "Authorized",
      viewTopics: viewTopics === "Authorized",
      requestItems: false,
      viewKafkaConnect: viewKafkaConnect === "Authorized",
      syncBackTopics: syncBackTopics === "Authorized",
      syncBackSchemas: syncBackSchemas === "Authorized",
      syncBackAcls: syncBackAcls === "Authorized",
      updateServerConfig: updateServerConfig === "Authorized",
      showServerConfigEnvProperties:
        showServerConfigEnvProperties === "Authorized",
      addUser: addUser === "Authorized",
      addTeams: addTeams === "Authorized",
      syncTopicsAcls: syncTopicsAcls === "Authorized",
      syncConnectors: syncConnectors === "Authorized",
      manageConnectors: false,
      syncSchemas: syncSchemas === "Authorized",
      approveAtleastOneRequest: approveAtleastOneRequest === "Authorized",
      approveDeclineTopics: false,
      approveDeclineOperationalReqs:
        approveDeclineOperationalReqs === "Authorized",
      approveDeclineSubscriptions: approveDeclineSubscriptions === "Authorized",
      approveDeclineSchemas: approveDeclineSchemas === "Authorized",
      approveDeclineConnectors: approveDeclineConnectors === "Authorized",
      showAddDeleteTenants: showAddDeleteTenants === "Authorized",
      addDeleteEditClusters: addDeleteEditClusters === "Authorized",
      addDeleteEditEnvs: addDeleteEditEnvs === "Authorized",
    },
  };
}

async function getAuth(): Promise<AuthUser> {
  return api
    .get<KlawApiResponse<"getAuth">>(API_PATHS.getAuth)
    .then((response) => transformAuthResponse(response));
}

function logoutUser() {
  return api.post<KlawApiResponse<"logout">, never>(API_PATHS.logout);
}

export { getAuth, logoutUser, isSuperAdmin };
