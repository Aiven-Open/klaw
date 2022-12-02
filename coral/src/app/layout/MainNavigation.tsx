import { Box } from "@aivenio/design-system";
import database from "@aivenio/design-system/dist/src/icons/database";
import codeBlock from "@aivenio/design-system/dist/src/icons/codeBlock";
import layoutGroupBy from "@aivenio/design-system/dist/src/icons/layoutGroupBy";
import code from "@aivenio/design-system/dist/src/icons/code";
import people from "@aivenio/design-system/dist/src/icons/people";
import list from "@aivenio/design-system/dist/src/icons/list";
import cog from "@aivenio/design-system/dist/src/icons/cog";
import MainNavigationLink from "src/app/layout/MainNavigationLink";

const originLocationKlawAngular = window.location.origin;

function MainNavigation() {
  return (
    <Box
      component={"nav"}
      backgroundColor={"grey-0"}
      aria-label={"Main navigation"}
      width={"full"}
      paddingTop={"l2"}
    >
      <ul>
        <li>
          <MainNavigationLink
            icon={database}
            href={`${originLocationKlawAngular}/index`}
            linkText={"Overview"}
          />
        </li>
        <li>
          <MainNavigationLink
            icon={codeBlock}
            href={`/topics`}
            linkText={"Topics"}
            active={true}
          />
        </li>
        <li>
          <MainNavigationLink
            icon={layoutGroupBy}
            href={`${originLocationKlawAngular}/kafkaConnectors`}
            linkText={"Kafka Connector"}
          />
        </li>
        <li>
          <MainNavigationLink icon={code} href={`/`} linkText={"Schemas"} />
        </li>
        <li>
          <MainNavigationLink
            icon={people}
            href={`/`}
            linkText={"Users and teams"}
          />
        </li>
        <li>
          <MainNavigationLink icon={list} href={`/`} linkText={"Audit log"} />
        </li>

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
        <li>
          <MainNavigationLink icon={cog} href={`/`} linkText={"Settings"} />
        </li>
      </ul>
    </Box>
  );
}

export default MainNavigation;
