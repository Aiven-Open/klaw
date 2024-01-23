import { Box, Divider } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import codeBlock from "@aivenio/aquarium/dist/src/icons/codeBlock";
import database from "@aivenio/aquarium/dist/src/icons/database";
import dataflow02 from "@aivenio/aquarium/dist/src/icons/dataflow02";
import list from "@aivenio/aquarium/dist/src/icons/list";
import person from "@aivenio/aquarium/dist/src/icons/person";
import settings from "@aivenio/aquarium/dist/src/icons/settings";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useLocation } from "react-router-dom";
import { TeamInfo } from "src/app/features/team-info/TeamInfo";
import { usePendingRequests } from "src/app/hooks/usePendingRequests";
import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import MainNavigationSubmenuList from "src/app/layout/main-navigation/MainNavigationSubmenuList";
import { Routes } from "src/app/router_utils";
import useFeatureFlag from "src/services/feature-flags/hook/useFeatureFlag";
import { FeatureFlag } from "src/services/feature-flags/types";

function MainNavigation() {
  const { pathname } = useLocation();
  const { TOTAL_NOTIFICATIONS } = usePendingRequests();
  const dashBoardEnabled = useFeatureFlag(FeatureFlag.FEATURE_FLAG_DASHBOARD);

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
            to={dashBoardEnabled ? Routes.DASHBOARD : "/index"}
            linkText={"Dashboard"}
            active={pathname === Routes.DASHBOARD}
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
            active={
              pathname.startsWith(Routes.CONNECTORS) ||
              pathname.startsWith("/connector")
            }
          />
        </li>
        <li>
          <MainNavigationLink
            icon={tickCircle}
            to={Routes.APPROVALS}
            linkText={"Approve requests"}
            active={pathname.startsWith(Routes.APPROVALS)}
            notifications={TOTAL_NOTIFICATIONS}
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
            to={Routes.ACTIVITY_LOG}
            linkText={"Activity log"}
            active={pathname.startsWith(Routes.ACTIVITY_LOG)}
          />
        </li>
        <Box aria-hidden={"true"} paddingTop={"l1"} paddingBottom={"l2"}>
          <Divider direction="horizontal" size={2} />
        </Box>
        <li>
          <MainNavigationSubmenuList
            icon={settings}
            text={"Configuration overview"}
            defaultExpanded={pathname.startsWith(Routes.CONFIGURATION)}
          >
            <MainNavigationLink
              to={Routes.USERS}
              linkText={"Users"}
              active={pathname.startsWith(Routes.USERS)}
            />
            <MainNavigationLink
              to={Routes.TEAMS}
              linkText={"Teams"}
              active={pathname.startsWith(Routes.TEAMS)}
            />
            <MainNavigationLink
              to={Routes.ENVIRONMENTS}
              linkText={"Environments"}
              active={pathname.startsWith(Routes.ENVIRONMENTS)}
            />
            <MainNavigationLink
              to={Routes.CLUSTERS}
              linkText={"Clusters"}
              active={pathname.startsWith(Routes.CLUSTERS)}
            />
          </MainNavigationSubmenuList>
        </li>
        <li>
          <MainNavigationSubmenuList
            icon={person}
            text={"User information"}
            defaultExpanded={pathname.startsWith(Routes.USER_INFORMATION)}
          >
            <MainNavigationLink
              to={Routes.USER_PROFILE}
              linkText={"User profile"}
              active={pathname.startsWith(Routes.USER_PROFILE)}
            />
            <MainNavigationLink
              to={Routes.USER_CHANGE_PASSWORD}
              linkText={"Change password"}
              active={pathname.startsWith(Routes.USER_CHANGE_PASSWORD)}
            />
            <MainNavigationLink
              to={Routes.USER_TENANT_INFO}
              linkText={"Tenant information"}
              active={pathname.startsWith(Routes.USER_TENANT_INFO)}
            />
          </MainNavigationSubmenuList>
        </li>
      </ul>
    </Box>
  );
}

export default MainNavigation;
