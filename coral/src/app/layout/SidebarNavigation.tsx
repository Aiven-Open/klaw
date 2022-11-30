import { Box, Flexbox, Icon } from "@aivenio/design-system";
import database from "@aivenio/design-system/dist/src/icons/database";
import codeBlock from "@aivenio/design-system/dist/src/icons/codeBlock";
import layoutGroupBy from "@aivenio/design-system/dist/src/icons/layoutGroupBy";
import code from "@aivenio/design-system/dist/src/icons/code";
import people from "@aivenio/design-system/dist/src/icons/people";
import list from "@aivenio/design-system/dist/src/icons/list";
import cog from "@aivenio/design-system/dist/src/icons/cog";
import data from "@aivenio/design-system/dist/src/icons/console";
import classes from "src/app/layout/SidebarNavigation.module.css";

const originLocationKlawAngular = window.location.origin;
function createSidebarListItem({
  icon,
  href,
  linkText,
  active = false,
}: {
  icon: typeof data;
  href: string;
  linkText: string;
  active?: boolean;
}) {
  return (
    <Flexbox
      htmlTag={"li"}
      alignItems={"center"}
      marginBottom={"l1"}
      colGap={"l1"}
      className={active ? classes.linkActive : classes.linkHover}
      paddingLeft={"l2"}
    >
      <Icon icon={icon} />
      <a href={href}>{linkText}</a>
    </Flexbox>
  );
}

function SidebarNavigation() {
  return (
    <Box
      component={"nav"}
      backgroundColor={"grey-0"}
      aria-label={"Primary navigation"}
      width={"full"}
      paddingTop={"l2"}
    >
      <ul>
        {createSidebarListItem({
          icon: database,
          href: `${originLocationKlawAngular}/index`,
          linkText: "Overview",
        })}
        {createSidebarListItem({
          icon: codeBlock,
          href: "/",
          linkText: "Topics",
          active: true,
        })}
        {createSidebarListItem({
          icon: layoutGroupBy,
          href: `${originLocationKlawAngular}/kafkaConnectors`,
          linkText: "Kafka Connector",
        })}

        {/*@TODO add right link after feedback */}
        {createSidebarListItem({
          icon: code,
          href: "/",
          linkText: "Schemas",
        })}

        {/*@TODO add right link after feedback */}
        {createSidebarListItem({
          icon: people,
          href: "/",
          linkText: "User and teams",
        })}

        {/*@TODO add right link after feedback */}
        {createSidebarListItem({
          icon: list,
          href: "/",
          linkText: "Audit log",
        })}

        {/*//@TODO ask DS about color options Divider*/}
        <li
          aria-hidden={"true"}
          className={"bg-grey-5"}
          style={{
            minHeight: "1px",
            marginBottom: "20px",
            marginTop: "20px",
          }}
        ></li>
        {createSidebarListItem({
          icon: cog,
          href: "/",
          linkText: "Settings",
        })}
      </ul>
    </Box>
  );
}

export default SidebarNavigation;
