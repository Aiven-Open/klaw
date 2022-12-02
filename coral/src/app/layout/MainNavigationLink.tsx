import { Box, Flexbox, Icon } from "@aivenio/design-system";
import data from "@aivenio/design-system/dist/src/icons/console";
import classes from "src/app/layout/MainNavigationLink.module.css";

type MainNavigationLinkProps = {
  icon: typeof data;
  href: string;
  linkText: string;
  active?: boolean;
};
function MainNavigationLink(props: MainNavigationLinkProps) {
  const { icon, href, linkText, active } = props;
  return (
    <Box
      marginBottom={"l1"}
      className={
        active ? classes.mainNavigationLinkActive : classes.mainNavigationLink
      }
      paddingLeft={"l2"}
      marginRight={"l2"}
    >
      <a href={href} aria-current={active && "page"}>
        <Flexbox
          htmlTag={"span"}
          direction={"row"}
          alignItems={"center"}
          colGap={"l1"}
        >
          <Icon icon={icon} />
          <span>{linkText}</span>
        </Flexbox>
      </a>
    </Box>
  );
}

export default MainNavigationLink;
