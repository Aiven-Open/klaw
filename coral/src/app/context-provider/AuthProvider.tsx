import { createContext, ReactNode, useContext } from "react";
import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuth, isSuperAdmin } from "src/domain/auth-user";
import { BasePage } from "src/app/layout/page/BasePage";
import { Box, Icon } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { NoCoralAccessSuperadmin } from "src/app/components/NoCoralAccessSuperadmin";
import {
  getRequestsWaitingForApproval,
  RequestsWaitingForApprovalWithTotal,
} from "src/domain/requests";

interface AuthContextValue {
  authUser: Omit<AuthUser, "totalTeamTopics" | "totalOrgTopics">;
  analytics: Pick<AuthUser, "totalTeamTopics" | "totalOrgTopics">;
  pendingRequests: RequestsWaitingForApprovalWithTotal;
}

/** We don't do Authentication on Corals side
 * at the moment, so we only have a AuthUser
 * in the context
 * */
const AuthContext = createContext<AuthContextValue>({
  authUser: {
    username: "",
    userrole: "",
    teamname: "",
    teamId: "",
    canSwitchTeams: "",
  },
  analytics: { totalTeamTopics: 0, totalOrgTopics: 0 },
  pendingRequests: {
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 0,
    CONNECTOR: 0,
    OPERATIONAL: 0,
    USER: 0,
    TOTAL_NOTIFICATIONS: 0,
  },
});

const useAuthContext = () => useContext(AuthContext);

const AuthProvider = ({ children }: { children: ReactNode }) => {
  const { data: authUser, isLoading: isLoadingAuthUser } = useQuery<
    AuthUser | undefined
  >(["user-getAuth-data"], getAuth);
  const { data: pendingRequests, isLoading: isLoadingPendingRequests } =
    useQuery(["getRequestsWaitingForApproval"], {
      queryFn: getRequestsWaitingForApproval,
    });

  // SUPERADMIN does not have access to coral, so we show a reduced page with
  // information about that and nothing else.
  if (
    !isLoadingAuthUser &&
    !isLoadingPendingRequests &&
    authUser &&
    isSuperAdmin(authUser)
  ) {
    return (
      <BasePage headerContent={<></>} content={<NoCoralAccessSuperadmin />} />
    );
  }

  if (
    !isLoadingAuthUser &&
    !isLoadingPendingRequests &&
    authUser &&
    pendingRequests
  ) {
    const authUserValue = {
      username: authUser.username,
      userrole: authUser.userrole,
      teamname: authUser.teamname,
      teamId: authUser.teamId,
      canSwitchTeams: authUser.canSwitchTeams,
    };
    const analyticsValue = {
      totalTeamTopics: authUser.totalTeamTopics,
      totalOrgTopics: authUser.totalOrgTopics,
    };

    return (
      <AuthContext.Provider
        value={{
          authUser: authUserValue,
          analytics: analyticsValue,
          pendingRequests,
        }}
      >
        {children}
      </AuthContext.Provider>
    );
  }

  return (
    <BasePage
      content={
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading Klaw</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      }
    />
  );
};

export { useAuthContext, AuthProvider };
