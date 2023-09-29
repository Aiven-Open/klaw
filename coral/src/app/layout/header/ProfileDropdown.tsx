import {
  Box,
  DropdownMenu,
  Typography,
  Icon,
  useToastContext,
} from "@aivenio/aquarium";
import user from "@aivenio/aquarium/dist/src/icons/user";
import logOut from "@aivenio/aquarium/dist/src/icons/logOut";
import classes from "src/app/layout/header/ProfileDropdown.module.css";
import { logoutUser } from "src/domain/auth-user";

type MenuItem = {
  path: string;
  name: string;
};

const menuItems: MenuItem[] = [
  {
    path: "/myProfile",
    name: "My profile",
  },
  {
    path: "/tenantInfo",
    name: "My tenant info",
  },
  {
    path: "/changePwd",
    name: "Change password",
  },
];

const LOGOUT_KEY = "logout";
function ProfileDropdown() {
  const [toast, dismiss] = useToastContext();
  function navigateToAngular(path: string) {
    window.location.assign(`${window.origin}${path}`);
  }

  function onDropdownClick(actionKey: string | number) {
    if (actionKey === LOGOUT_KEY) {
      toast({
        id: "logout",
        message: "You are being logged out of Klaw...",
        position: "bottom-left",
        variant: "default",
      });
      logoutUser().catch((error) => {
        dismiss("logout");
        if (error.status !== 401) {
          toast({
            message:
              "Something went wrong in the log out process. Please try again or contact your administrator.",
            position: "bottom-left",
            variant: "danger",
          });
          console.error(error);
        } else {
          window.location.assign(`${window.origin}/login`);
        }
      });
      return;
    } else {
      const selectedItem = menuItems[actionKey as number];
      // selectedItem should never be undefined, this is to support
      // developers and make sure we can not make a mistake here
      if (selectedItem === undefined) {
        console.error(`No item with index ${actionKey} found.`);
      } else {
        navigateToAngular(selectedItem.path);
      }
    }
  }

  return (
    <DropdownMenu
      onAction={onDropdownClick}
      maxWidth={300}
      placement="bottom-right"
      header={
        <Box.Flex backgroundColor={"primary-10"} paddingLeft={"l1"}>
          <Box paddingTop={"l1"} className={"profile-dropdown-header"}>
            <Typography.Large color="grey-90" className="panel-header-title">
              User name
            </Typography.Large>
            <Typography.Caption color="grey-50">Team</Typography.Caption>
          </Box>
          <Icon
            color={"primary-30"}
            icon={user}
            height={80}
            width={80}
            className={classes.profileDropdownHeaderIcon}
          />
        </Box.Flex>
      }
    >
      <DropdownMenu.Trigger>
        <button aria-label={"Open profile menu"}>
          <Icon icon={user} fontSize={"20px"} color={"grey-0"} />
        </button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Items>
        <DropdownMenu.Section>
          {menuItems.map((item, index) => {
            return (
              <DropdownMenu.Item key={index}>{item.name}</DropdownMenu.Item>
            );
          })}
        </DropdownMenu.Section>
        <DropdownMenu.Section>
          <DropdownMenu.Item key={LOGOUT_KEY} icon={logOut}>
            Log out
          </DropdownMenu.Item>
        </DropdownMenu.Section>
      </DropdownMenu.Items>
    </DropdownMenu>
  );
}

export { ProfileDropdown };
