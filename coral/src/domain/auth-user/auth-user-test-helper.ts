import { AuthUser } from "src/domain/auth-user/auth-user-types";

const testAuthUser: AuthUser = {
  canSwitchTeams: "false",
  teamId: "1234567",
  teamname: "awesome-bunch-of-people",
  username: "i-am-test-user",
  userrole: "USER",
  totalTeamTopics: 0,
  totalOrgTopics: 0,
};

export { testAuthUser };
