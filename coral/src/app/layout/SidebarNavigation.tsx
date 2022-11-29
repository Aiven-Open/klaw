import { Flexbox, Icon } from "@aivenio/design-system";
import database from "@aivenio/design-system/dist/src/icons/database";
import codeBlock from "@aivenio/design-system/dist/src/icons/codeBlock";
import layoutGroupBy from "@aivenio/design-system/dist/src/icons/layoutGroupBy";
import code from "@aivenio/design-system/dist/src/icons/code";
import people from "@aivenio/design-system/dist/src/icons/people";
import alignJustify from "@aivenio/design-system/dist/src/icons/alignJustify";
import cog from "@aivenio/design-system/dist/src/icons/cog";

// This is a WIP placeholder
// that does not have real data yet
function SidebarNavigation() {
  return (
    <Flexbox backgroundColor={"grey-5"} className={"p-6"}>
      <nav
        aria-label={"Primary navigation"}
        style={{
          width: "100%",
        }}
      >
        <ul>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={database} className={"mr-4"} />
            <a href="/">Overview</a>
          </Flexbox>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={codeBlock} className={"mr-4"} />
            <a href="/" aria-current="page">
              Topics
            </a>
          </Flexbox>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={layoutGroupBy} className={"mr-4"} />
            <a href="/">Kafka Connector</a>
          </Flexbox>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={code} className={"mr-4"} />
            <a href="/">Schemas</a>
          </Flexbox>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={people} className={"mr-4"} />
            <a href="/">User and teams</a>
          </Flexbox>
          {/*@TODO WRONG ICON, couldn't find it, wait for Figma designs*/}
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={alignJustify} className={"mr-4"} />
            <a href="/">Audit log</a>
          </Flexbox>
          <li
            aria-hidden={"true"}
            className={"bg-grey-20"}
            style={{
              minHeight: "1px",
              marginBottom: "20px",
              marginTop: "20px",
            }}
          ></li>
          <Flexbox htmlTag={"li"} alignItems={"center"} className={"my-4"}>
            <Icon icon={cog} className={"mr-4"} />
            <a href="/">Settings</a>
          </Flexbox>
        </ul>
      </nav>
    </Flexbox>
  );
}

export default SidebarNavigation;
