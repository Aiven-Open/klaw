import {
  getAuth,
  logoutUser,
  isSuperAdmin,
} from "src/domain/auth-user/auth-user-api";
import { AuthUser } from "src/domain/auth-user/auth-user-types";

export { getAuth, logoutUser, isSuperAdmin };
export type { AuthUser };
