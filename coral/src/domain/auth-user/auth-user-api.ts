import {
  AuthUser,
  AuthUserLoginData,
} from "src/domain/auth-user/auth-user-types";

import api from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getAuthUser = (userLogin: AuthUserLoginData): Promise<AuthUser> => {
  const data = new URLSearchParams();
  data.append("username", userLogin.username);
  data.append("password", userLogin.password);

  return api.post("/login", data);
};

function getUserTeamName(): Promise<string> {
  return api
    .get<KlawApiResponse<"getAuth">>("/getAuth")
    .then((response) => response.teamname);
}

export { getAuthUser, getUserTeamName };
