import { Box, Icon } from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/console";
import { Link } from "react-router-dom";
import classes from "src/app/layout/main-navigation/MainNavigationLink.module.css";

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
  href: string;
  useRouter?: boolean;
  linkText: string;
  active?: boolean;
};
function MainNavigationLink(props: MainNavigationLinkProps) {
  const { icon, href, linkText, active, useRouter = false } = props;
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
      {useRouter ? (
        <Link to={href} aria-current={active && "page"}>
          <LinkContent icon={icon} linkText={linkText} />
        </Link>
      ) : (
        <a href={href} aria-current={active && "page"}>
          <LinkContent icon={icon} linkText={linkText} />
        </a>
      )}
    </Box>
  );
}

export default MainNavigationLink;
