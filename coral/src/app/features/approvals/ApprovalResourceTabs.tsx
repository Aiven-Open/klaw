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
  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab title="Topics" value={ApprovalsTabEnum.TOPICS} badge={99}>
        {currentTab === ApprovalsTabEnum.TOPICS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab title="ACLs" value={ApprovalsTabEnum.ACLS} badge={99}>
        {currentTab === ApprovalsTabEnum.ACLS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab title="Schemas" value={ApprovalsTabEnum.SCHEMAS} badge={99}>
        {currentTab === ApprovalsTabEnum.SCHEMAS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab title="Connectors" value={ApprovalsTabEnum.CONNECTORS}>
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
}

export default ApprovalResourceTabs;
