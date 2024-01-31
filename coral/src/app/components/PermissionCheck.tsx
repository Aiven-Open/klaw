import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { Permission } from "src/domain/auth-user/auth-user-types";

const PermissionCheck = ({
  permission,
  children,
  placeholder,
}: {
  permission: Permission;
  children: React.ReactNode;
  placeholder?: React.ReactNode;
}) => {
  const { permissions } = useAuthContext();
  const isAuthorized = permissions[permission];

  if (placeholder === undefined) {
    return isAuthorized ? children : null;
  }

  return isAuthorized ? children : placeholder;
};

export default PermissionCheck;
