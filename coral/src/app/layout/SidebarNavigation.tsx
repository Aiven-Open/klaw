import { Box, Flexbox, Icon } from "@aivenio/design-system";
import database from "@aivenio/design-system/dist/src/icons/database";
import codeBlock from "@aivenio/design-system/dist/src/icons/codeBlock";
import layoutGroupBy from "@aivenio/design-system/dist/src/icons/layoutGroupBy";
import code from "@aivenio/design-system/dist/src/icons/code";
import people from "@aivenio/design-system/dist/src/icons/people";
import alignJustify from "@aivenio/design-system/dist/src/icons/alignJustify";
import cog from "@aivenio/design-system/dist/src/icons/cog";
import data from "@aivenio/design-system/dist/src/icons/console";

function createSidebarListItem({
  icon,
  href,
  linkText,
}: {
  icon: typeof data;
  href: string;
  linkText: string;
}) {
  return (
    <Flexbox
      htmlTag={"li"}
      alignItems={"center"}
      marginLeft={"l1"}
      marginBottom={"l1"}
      colGap={"l1"}
    >
      <Icon icon={icon} />
      <a href={href}>{linkText}</a>
    </Flexbox>
  );
}

function SidebarNavigation() {
  return (
    <Flexbox backgroundColor={"grey-5"} padding={"l3"}>
      <Box component={"nav"} aria-label={"Primary navigation"} width={"full"}>
        <ul>
          {createSidebarListItem({
            icon: database,
            href: "/",
            linkText: "Overview",
          })}
          {createSidebarListItem({
            icon: codeBlock,
            href: "/",
            linkText: "Topics",
          })}
          {createSidebarListItem({
            icon: layoutGroupBy,
            href: "/",
            linkText: "Kafka Connector",
          })}

          {createSidebarListItem({
            icon: code,
            href: "/",
            linkText: "Schemas",
          })}

          {createSidebarListItem({
            icon: people,
            href: "/",
            linkText: "User and teams",
          })}

          {/*@TODO WRONG ICON, couldn't find it, wait for Figma designs*/}
          {createSidebarListItem({
            icon: alignJustify,
            href: "/",
            linkText: "Audit log",
          })}

          {/*//@TODO ask DS about color options Divider*/}
          <li
            aria-hidden={"true"}
            className={"bg-grey-20"}
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
    </Flexbox>
  );
}

export default SidebarNavigation;
