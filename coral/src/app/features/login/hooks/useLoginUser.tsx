import { useMutation } from "@tanstack/react-query";
import { getAuthUserMockForLogin } from "src/domain/auth-user";
import { AuthUserLoginData } from "src/domain/auth-user/auth-user-types";

function useLoginUser() {
  return useMutation((userLogin: AuthUserLoginData) => {
    return getAuthUserMockForLogin(userLogin);
  });
}

export { useLoginUser };
