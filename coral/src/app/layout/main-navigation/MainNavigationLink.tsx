import { Box, Icon, Link } from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/console";
import { TabBadge } from "@aivenio/aquarium/dist/src/molecules/Badge/Badge";
import { Link as RouterLink } from "react-router-dom";
import classes from "src/app/layout/main-navigation/MainNavigationLink.module.css";
import { Routes } from "src/app/router_utils";

function LinkContent({
  linkText,
  icon,
}: Pick<MainNavigationLinkProps, "linkText" | "icon">) {
  return (
    <Box
      component={"span"}
      display={"flex"}
      flexDirection={"row"}
      alignItems={"center"}
      colGap={"l1"}
    >
      {icon && <Icon icon={icon} />}
      <span>{linkText}</span>
    </Box>
  );
}

type MainNavigationLinkProps = {
  icon?: typeof data;
  // only use a string if the link
  // should go into the Klaw Angular app!
  to: Routes | string;
  linkText: string;
  active?: boolean;
  notifications?: number;
};
function MainNavigationLink(props: MainNavigationLinkProps) {
  const { icon, to, linkText, active = false, notifications = 0 } = props;

  function isRouterLink() {
    const allRoutes: string[] = Object.values(Routes);
    return allRoutes.includes(to);
  }

  return (
    <Box.Flex
      className={
        active ? classes.mainNavigationLinkActive : classes.mainNavigationLink
      }
      marginTop={"-3"}
      paddingTop={"3"}
      paddingRight={"l2"}
      paddingLeft={"l5"}
      marginLeft={"-l3"}
      marginBottom={"3"}
      paddingBottom={"3"}
      justifyContent={"space-between"}
      gap={"1"}
    >
      {isRouterLink() ? (
        <RouterLink to={to} aria-current={active && "page"}>
          <LinkContent icon={icon} linkText={linkText} />
          {notifications > 0 && (
            <span className={"visually-hidden"}>
              {`${notifications} pending requests.`}
            </span>
          )}
        </RouterLink>
      ) : (
        <Link href={to} aria-current={active && "page"}>
          <LinkContent icon={icon} linkText={linkText} />
          {notifications > 0 && (
            <span className={"visually-hidden"}>
              {`${notifications} pending requests.`}
            </span>
          )}
        </Link>
      )}
      {notifications > 0 && (
        <Box.Flex
          alignItems="center"
          color={"var(--aquarium-colors-primary-80)"}
        >
          <Box.Flex height={"fit"}>
            <TabBadge value={notifications} />
          </Box.Flex>
        </Box.Flex>
      )}
    </Box.Flex>
  );
}

export default MainNavigationLink;
