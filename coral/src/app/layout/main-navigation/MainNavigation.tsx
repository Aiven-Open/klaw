import { Box, Divider, Flexbox } from "@aivenio/aquarium";
import codeBlock from "@aivenio/aquarium/dist/src/icons/codeBlock";
import cog from "@aivenio/aquarium/dist/src/icons/cog";
import add from "@aivenio/aquarium/dist/src/icons/add";
import database from "@aivenio/aquarium/dist/src/icons/database";
import layoutGroupBy from "@aivenio/aquarium/dist/src/icons/layoutGroupBy";
import list from "@aivenio/aquarium/dist/src/icons/list";
import people from "@aivenio/aquarium/dist/src/icons/people";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useLocation } from "react-router-dom";
import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import MainNavigationSubmenuList from "src/app/layout/main-navigation/MainNavigationSubmenuList";
import { Routes } from "src/app/router_utils";
import { useQuery } from "@tanstack/react-query";
import { getUserTeamName } from "src/domain/auth-user";

function MainNavigation() {
  const { pathname } = useLocation();

  const { data: teamName, isLoading } = useQuery(
    ["user-getAuth-data"],
    getUserTeamName
  );

  const getUserTeam = () => {
    if (isLoading) {
      return <i className="text-grey-40">Fetching team...</i>;
    }
    if (!isLoading && teamName !== undefined) {
      return teamName;
    }
    return <i>No team found</i>;
  };

  return (
    <Box
      component={"nav"}
      backgroundColor={"grey-0"}
      aria-label={"Main navigation"}
      width={"full"}
      minHeight={"full"}
      paddingTop={"l2"}
    >
      <Flexbox direction={"column"} paddingLeft={"l3"}>
        <div className="inline-block mb-2 typography-small-strong text-grey-60">
          Team
        </div>
        <div>{getUserTeam()}</div>
      </Flexbox>
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
            icon={layoutGroupBy}
            to={Routes.CONNECTORS}
            linkText={"Kafka Connectors"}
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
