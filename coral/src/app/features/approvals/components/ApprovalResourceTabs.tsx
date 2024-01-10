import { Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import {
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  Routes,
  isApprovalsTabEnum,
} from "src/app/router_utils";

type Props = {
  currentTab: ApprovalsTabEnum;
};

function ApprovalResourceTabs({ currentTab }: Props) {
  const navigate = useNavigate();

  const { pendingRequests } = useAuthContext();

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title="Topics"
        value={ApprovalsTabEnum.TOPICS}
        badge={getBadgeValue(pendingRequests.TOPIC)}
        aria-label={getTabAriaLabel("Topics", pendingRequests.TOPIC)}
      >
        {currentTab === ApprovalsTabEnum.TOPICS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="ACLs"
        value={ApprovalsTabEnum.ACLS}
        badge={getBadgeValue(pendingRequests.ACL)}
        aria-label={getTabAriaLabel("ACLs", pendingRequests.ACL)}
      >
        {currentTab === ApprovalsTabEnum.ACLS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Schemas"
        value={ApprovalsTabEnum.SCHEMAS}
        badge={getBadgeValue(pendingRequests.SCHEMA)}
        aria-label={getTabAriaLabel("Schemas", pendingRequests.SCHEMA)}
      >
        {currentTab === ApprovalsTabEnum.SCHEMAS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Connectors"
        value={ApprovalsTabEnum.CONNECTORS}
        badge={getBadgeValue(pendingRequests.CONNECTOR)}
        aria-label={getTabAriaLabel("Connectors", pendingRequests.CONNECTOR)}
      >
        {currentTab === ApprovalsTabEnum.CONNECTORS && <Outlet />}
      </Tabs.Tab>
    </Tabs>
  );

  function navigateToTab(
    navigate: NavigateFunction,
    resourceTypeId: unknown
  ): void {
    if (isApprovalsTabEnum(resourceTypeId)) {
      navigate(
        `${Routes.APPROVALS}/${APPROVALS_TAB_ID_INTO_PATH[resourceTypeId]}`,
        {
          replace: true,
        }
      );
    }
  }

  function getTabAriaLabel(
    title: string,
    pendingApprovals: number | undefined
  ): string {
    if (typeof pendingApprovals === "number") {
      if (pendingApprovals === 0) {
        return `${title}, no pending approvals`;
      } else if (pendingApprovals === 1) {
        return `${title}, ${pendingApprovals} approval waiting`;
      } else {
        return `${title}, ${pendingApprovals} approvals waiting`;
      }
    }
    return title;
  }

  function getBadgeValue(
    pendingApprovals: number | undefined
  ): number | undefined {
    if (typeof pendingApprovals === "number" && pendingApprovals > 0) {
      return pendingApprovals;
    }
    return undefined;
  }
}

export default ApprovalResourceTabs;
