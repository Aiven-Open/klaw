import { Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  ApprovalsTabEnum,
  APPROVALS_TAB_ID_INTO_PATH,
  isApprovalsTabEnum,
} from "src/app/router_utils";

type Props = {
  currentTab: ApprovalsTabEnum;
};

function ApprovalResourceTabs({ currentTab }: Props) {
  const navigate = useNavigate();

  const numberOfPendingTopicApprovals = 99;
  const numberOfPendingAclApprovals = 33;
  const numberOfPendingSchemaApprovals = undefined;
  const numberOfPendingConnectorApprovals = 0;
  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title="Topics"
        value={ApprovalsTabEnum.TOPICS}
        badge={getBadgeValue(numberOfPendingTopicApprovals)}
        aria-label={getTabAriaLabel("Topics", numberOfPendingTopicApprovals)}
      >
        {currentTab === ApprovalsTabEnum.TOPICS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="ACLs"
        value={ApprovalsTabEnum.ACLS}
        badge={getBadgeValue(numberOfPendingAclApprovals)}
        aria-label={getTabAriaLabel("ACLs", numberOfPendingAclApprovals)}
      >
        {currentTab === ApprovalsTabEnum.ACLS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Schemas"
        value={ApprovalsTabEnum.SCHEMAS}
        badge={getBadgeValue(numberOfPendingSchemaApprovals)}
        aria-label={getTabAriaLabel("Schemas", numberOfPendingSchemaApprovals)}
      >
        {currentTab === ApprovalsTabEnum.SCHEMAS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Connectors"
        value={ApprovalsTabEnum.CONNECTORS}
        badge={getBadgeValue(numberOfPendingSchemaApprovals)}
        aria-label={getTabAriaLabel(
          "Connectors",
          numberOfPendingConnectorApprovals
        )}
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
    if (typeof pendingApprovals === "number" && pendingApprovals > 0) {
      return `${title}, ${pendingApprovals} approvals waiting`;
    }
    return `${title}, no pending approvals`;
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
