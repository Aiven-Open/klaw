import { Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
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
        {currentTab === RequestsTabEnum.CONNECTORS && <Outlet />}
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
