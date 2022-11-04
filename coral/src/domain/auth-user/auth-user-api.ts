import {
  AuthUser,
  AuthUserLoginData,
} from "src/domain/auth-user/auth-user-types";

const getAuthUser = async (userLogin: AuthUserLoginData): Promise<AuthUser> => {
  return fetch("/user/authenticate", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(userLogin),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      return response.json();
    })
    .catch((error) => {
      throw new Error(error.message);
    });
};

export { getAuthUser };
