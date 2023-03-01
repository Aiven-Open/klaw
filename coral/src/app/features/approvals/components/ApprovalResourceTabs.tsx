import { Tabs } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  ApprovalsTabEnum,
  APPROVALS_TAB_ID_INTO_PATH,
  isApprovalsTabEnum,
} from "src/app/router_utils";
import { getRequestsWaitingForApproval } from "src/domain/requests/requests-api";

type Props = {
  currentTab: ApprovalsTabEnum;
};

function ApprovalResourceTabs({ currentTab }: Props) {
  const navigate = useNavigate();

  const { data } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title="Topics"
        value={ApprovalsTabEnum.TOPICS}
        badge={getBadgeValue(data?.TOPIC)}
        aria-label={getTabAriaLabel("Topics", data?.TOPIC)}
      >
        {currentTab === ApprovalsTabEnum.TOPICS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="ACLs"
        value={ApprovalsTabEnum.ACLS}
        badge={getBadgeValue(data?.ACL)}
        aria-label={getTabAriaLabel("ACLs", data?.ACL)}
      >
        {currentTab === ApprovalsTabEnum.ACLS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Schemas"
        value={ApprovalsTabEnum.SCHEMAS}
        badge={getBadgeValue(data?.SCHEMA)}
        aria-label={getTabAriaLabel("Schemas", data?.SCHEMA)}
      >
        {currentTab === ApprovalsTabEnum.SCHEMAS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Connectors (coming soon)"
        value={ApprovalsTabEnum.CONNECTORS}
        badge={getBadgeValue(data?.CONNECTOR)}
        aria-label={getTabAriaLabel("Connectors", data?.CONNECTOR)}
        disabled
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
      navigate(`/approvals/${APPROVALS_TAB_ID_INTO_PATH[resourceTypeId]}`, {
        replace: true,
      });
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
