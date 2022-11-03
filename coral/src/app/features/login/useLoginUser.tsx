import { useMutation } from "@tanstack/react-query";
import { AuthUser, getAuthUser } from "src/domain/auth-user";
import { useEffect } from "react";
import { mockUserAuthRequest } from "src/domain/auth-user/auth-user-api.msw";

export default function useLoginUser() {
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      const mswInstance = browserEnvWorker;
      const user: AuthUser = {
        name: "Super Admin",
      };
      mockUserAuthRequest({ mswInstance, userResponse: user });
    }
  }, []);

  return useMutation(
    ({ username, password }: { username: string; password: string }) => {
      if (username !== "superadmin") {
        return Promise.reject(new Error("wrong username"));
      } else {
        return getAuthUser({ username, password });
      }
    }
  );
}

export { useLoginUser };
