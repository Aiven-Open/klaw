import { AuthUser } from "src/domain/auth-user/auth-user-types";

const getAuthUser = async ({
  username,
  password,
}: {
  username: string;
  password: string;
}): Promise<AuthUser> => {
  const res = await fetch("/user/authenticate", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  return res.json();
};

export { getAuthUser };
