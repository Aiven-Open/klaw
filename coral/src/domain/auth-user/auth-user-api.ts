import { AuthUser } from "src/domain/auth-user/auth-user-types";

const getAuthUser = async (): Promise<AuthUser> => {
  const res = await fetch("/user/authenticate", {
    method: "POST",
  });
  return res.json();
};

export { getAuthUser };
