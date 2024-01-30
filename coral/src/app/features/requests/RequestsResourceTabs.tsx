import { EmptyState, Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import PermissionsCheck from "src/app/components/PermissionsCheck";
import {
  RequestsTabEnum,
  isRequestsTabEnum,
  REQUESTS_TAB_ID_INTO_PATH,
} from "src/app/router_utils";

type Props = {
  currentTab: RequestsTabEnum;
};

function RequestsResourceTabs({ currentTab }: Props) {
  const navigate = useNavigate();

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title="Topics"
        value={RequestsTabEnum.TOPICS}
        aria-label={"Topic requests"}
      >
        {currentTab === RequestsTabEnum.TOPICS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="ACLs"
        value={RequestsTabEnum.ACLS}
        aria-label={"ACL requests"}
      >
        {currentTab === RequestsTabEnum.ACLS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Schemas"
        value={RequestsTabEnum.SCHEMAS}
        aria-label={"Schema requests"}
      >
        {currentTab === RequestsTabEnum.SCHEMAS && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Connectors"
        value={RequestsTabEnum.CONNECTORS}
        aria-label={"Connector requests"}
      >
        {/* Because Tabs doesn't allow to pass anything else than Tabs.Tab as child :| */}
        <PermissionsCheck
          permission="manageConnectors"
          placeholder={
            <EmptyState title="Not authorized">
              You are not authorized to manage connectors.
            </EmptyState>
          }
        >
          {currentTab === RequestsTabEnum.CONNECTORS && <Outlet />}
        </PermissionsCheck>
      </Tabs.Tab>
    </Tabs>
  );

  function navigateToTab(
    navigate: NavigateFunction,
    resourceTypeId: unknown
  ): void {
    if (isRequestsTabEnum(resourceTypeId)) {
      navigate(`/requests/${REQUESTS_TAB_ID_INTO_PATH[resourceTypeId]}`, {
        replace: true,
      });
    }
  }
}

export default RequestsResourceTabs;
