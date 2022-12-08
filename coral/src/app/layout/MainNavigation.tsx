import { Box } from "@aivenio/design-system";
import database from "@aivenio/design-system/dist/src/icons/database";
import codeBlock from "@aivenio/design-system/dist/src/icons/codeBlock";
import layoutGroupBy from "@aivenio/design-system/dist/src/icons/layoutGroupBy";
import people from "@aivenio/design-system/dist/src/icons/people";
import list from "@aivenio/design-system/dist/src/icons/list";
import cog from "@aivenio/design-system/dist/src/icons/cog";
import MainNavigationLink from "src/app/layout/MainNavigationLink";
import MainNavigationSubmenuList from "src/app/layout/MainNavigationSubmenuList";

function MainNavigation() {
  return (
    <Box
      component={"nav"}
      backgroundColor={"grey-0"}
      aria-label={"Main navigation"}
      width={"full"}
      minHeight={"full"}
      paddingTop={"l2"}
    >
      <ul>
        <li>
          <MainNavigationLink
            icon={database}
            href={`/index`}
            linkText={"Overviews"}
          />
        </li>
        <li>
          <MainNavigationSubmenuList
            expanded={true}
            icon={codeBlock}
            text={"Topics"}
          >
            <MainNavigationLink
              href={`/topics`}
              linkText={"All Topics"}
              active={true}
            />

            <MainNavigationLink
              href={`/execTopics`}
              linkText={"Approval Requests"}
            />

            <MainNavigationLink
              href={`/myTopicRequests`}
              linkText={"My Team's Requests"}
            />
          </MainNavigationSubmenuList>
        </li>
        <li>
          <MainNavigationSubmenuList
            icon={layoutGroupBy}
            text={"Kafka Connector"}
          >
            <MainNavigationLink
              href={`/kafkaConnectors`}
              linkText={"All Connectors"}
            />
            <MainNavigationLink
              href={`/kafkaConnectors`}
              linkText={"Connectors Requests"}
            />
          </MainNavigationSubmenuList>
        </li>
        <MainNavigationSubmenuList icon={people} text={"Users and Teams"}>
          <MainNavigationLink href={`/users`} linkText={"Users"} />
          <MainNavigationLink href={`/teams`} linkText={"Teams"} />
          <MainNavigationLink href={`/execUsers`} linkText={"User Requests"} />
        </MainNavigationSubmenuList>

        <li>
          <MainNavigationLink
            icon={list}
            href={`/activityLog`}
            linkText={"Audit Log"}
          />
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
          <MainNavigationLink
            icon={cog}
            href={`/serverConfig`}
            linkText={"Settings"}
          />
        </li>
      </ul>
    </Box>
  );
}

export default MainNavigation;
