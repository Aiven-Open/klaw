import {
  AuthUser,
  AuthUserLoginData,
} from "src/domain/auth-user/auth-user-types";

import api from "src/services/api";

const getAuthUser = (userLogin: AuthUserLoginData): Promise<AuthUser> => {
  const data = new URLSearchParams();
  data.append("username", userLogin.username);
  data.append("password", userLogin.password);

  return api.post("/login", data);
};

export { getAuthUser };
