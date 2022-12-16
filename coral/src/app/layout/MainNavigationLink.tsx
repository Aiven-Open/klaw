import { Box, Icon } from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/console";
import classes from "src/app/layout/main-navigation/MainNavigationLink.module.css";

type MainNavigationLinkProps = {
  icon?: typeof data;
  href: string;
  linkText: string;
  active?: boolean;
};
function MainNavigationLink(props: MainNavigationLinkProps) {
  const { icon, href, linkText, active } = props;
  return (
    <Box
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
    >
      <a href={href} aria-current={active && "page"}>
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
      </a>
    </Box>
  );
}

export default MainNavigationLink;
