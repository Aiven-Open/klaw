import { useMutation } from "@tanstack/react-query";
import { getAuthUser } from "src/domain/auth-user";
import { useEffect } from "react";
import { mockUserAuthRequest } from "src/domain/auth-user/auth-user-api.msw";
import { AuthUserLoginData } from "src/domain/auth-user/auth-user-types";

export default function useLoginUser() {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockUserAuthRequest(browserEnvWorker);
    }
  }, []);

  return useMutation((userLogin: AuthUserLoginData) => {
    return getAuthUser(userLogin);
  });
}

export { useLoginUser };
