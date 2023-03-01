import { Box, Divider } from "@aivenio/aquarium";
import codeBlock from "@aivenio/aquarium/dist/src/icons/codeBlock";
import cog from "@aivenio/aquarium/dist/src/icons/cog";
import database from "@aivenio/aquarium/dist/src/icons/database";
import layoutGroupBy from "@aivenio/aquarium/dist/src/icons/layoutGroupBy";
import list from "@aivenio/aquarium/dist/src/icons/list";
import people from "@aivenio/aquarium/dist/src/icons/people";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useLocation } from "react-router-dom";
import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import MainNavigationSubmenuList from "src/app/layout/main-navigation/MainNavigationSubmenuList";
import { Routes } from "src/app/router_utils";

function MainNavigation() {
  const { pathname } = useLocation();

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
            to={`/index`}
            linkText={"Dashboard"}
          />
        </li>
        <li>
          <MainNavigationSubmenuList
            expanded={true}
            icon={codeBlock}
            text={"Topics"}
          >
            <MainNavigationLink
              linkText={"All Topics"}
              to={Routes.TOPICS}
              active={
                pathname.startsWith(Routes.TOPICS) ||
                pathname.startsWith("/topic")
              }
            />
            <MainNavigationLink
              to={`/myTopicRequests`}
              linkText={"My team's requests"}
            />
          </MainNavigationSubmenuList>
        </li>
        <li>
          <MainNavigationSubmenuList
            icon={layoutGroupBy}
            text={"Kafka Connectors"}
          >
            <MainNavigationLink
              to={`/kafkaConnectors`}
              linkText={"All Connectors"}
            />
            <MainNavigationLink
              to={`/execConnectors`}
              linkText={"Connector requests"}
            />
          </MainNavigationSubmenuList>
        </li>
        <li>
          <MainNavigationSubmenuList icon={people} text={"Users and teams"}>
            <MainNavigationLink to={`/users`} linkText={"Users"} />
            <MainNavigationLink to={`/teams`} linkText={"Teams"} />
            <MainNavigationLink to={`/execUsers`} linkText={"User requests"} />
          </MainNavigationSubmenuList>
        </li>
        <li>
          <MainNavigationLink
            icon={tickCircle}
            to={Routes.APPROVALS}
            linkText={"Approve requests"}
            active={pathname.startsWith(Routes.APPROVALS)}
          />
        </li>
        <li>
          <MainNavigationLink
            icon={list}
            to={`/activityLog`}
            linkText={"Audit log"}
          />
        </li>
        <li>
          <Box aria-hidden={"true"} paddingTop={"l1"} paddingBottom={"l2"}>
            <Divider direction="horizontal" size={2} />
          </Box>
          <MainNavigationLink
            icon={cog}
            to={`/serverConfig`}
            linkText={"Settings"}
          />
        </li>
      </ul>
    </Box>
  );
}

export default MainNavigation;
