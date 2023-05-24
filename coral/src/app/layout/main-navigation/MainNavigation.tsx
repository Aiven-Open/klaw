import { Box, Divider } from "@aivenio/aquarium";
import codeBlock from "@aivenio/aquarium/dist/src/icons/codeBlock";
import cog from "@aivenio/aquarium/dist/src/icons/cog";
import add from "@aivenio/aquarium/dist/src/icons/add";
import database from "@aivenio/aquarium/dist/src/icons/database";
import dataflow02 from "@aivenio/aquarium/dist/src/icons/dataflow02";
import list from "@aivenio/aquarium/dist/src/icons/list";
import people from "@aivenio/aquarium/dist/src/icons/people";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useLocation } from "react-router-dom";
import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import MainNavigationSubmenuList from "src/app/layout/main-navigation/MainNavigationSubmenuList";
import { Routes } from "src/app/router_utils";
import { TeamInfo } from "src/app/features/team-info/TeamInfo";

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
      <Box paddingX={"l3"}>
        <TeamInfo />
      </Box>
      <Box aria-hidden={"true"} paddingTop={"l1"} paddingBottom={"l2"}>
        <Divider direction="horizontal" size={2} />
      </Box>
      <ul>
        <li>
          <MainNavigationLink
            icon={database}
            to={`/index`}
            linkText={"Dashboard"}
          />
        </li>
        <li>
          <MainNavigationLink
            icon={codeBlock}
            linkText={"Topics"}
            to={Routes.TOPICS}
            active={
              pathname.startsWith(Routes.TOPICS) ||
              pathname.startsWith("/topic")
            }
          />
        </li>
        <li>
          <MainNavigationLink
            icon={dataflow02}
            to={Routes.CONNECTORS}
            linkText={"Connectors"}
            active={pathname.startsWith(Routes.CONNECTORS)}
          />
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
            icon={add}
            linkText={"My team's requests"}
            to={Routes.REQUESTS}
            active={pathname.startsWith(Routes.REQUESTS)}
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
