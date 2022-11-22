import { useMutation } from "@tanstack/react-query";
import { getAuthUser } from "src/domain/auth-user";
import { AuthUserLoginData } from "src/domain/auth-user/auth-user-types";

function useLoginUser() {
  return useMutation((userLogin: AuthUserLoginData) => {
    return getAuthUser(userLogin);
  });
}

export { useLoginUser };
