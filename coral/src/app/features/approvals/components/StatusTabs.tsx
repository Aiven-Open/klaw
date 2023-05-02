import { Tabs } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { Outlet } from "react-router-dom";
import {
  isStatusName,
  statusNames,
} from "src/app/features/approvals/utils/request-status-helper";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { getRequestsWaitingForApproval } from "src/domain/requests/requests-api";

type StatusTabProps = {
  entity: "TOPIC" | "ACL" | "SCHEMA" | "CONNECTOR" | "USER";
};

function StatusTabs({ entity }: StatusTabProps) {
  const { status, setFilterValue } = useFiltersValues({
    defaultStatus: statusNames.CREATED,
  });
  const { data } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  const badgeValue =
    data !== undefined ? getBadgeValue(data[entity]) : undefined;

  return (
    <>
      <Tabs
        value={status}
        onChange={(statusName) => {
          if (isStatusName(statusName)) {
            return setFilterValue({ name: "status", value: statusName });
          }
        }}
      >
        <Tabs.Tab
          title="Awaiting approval"
          value={statusNames.CREATED}
          badge={badgeValue}
          aria-label={getTabAriaLabel(entity, badgeValue)}
        ></Tabs.Tab>
        <Tabs.Tab
          title="Approved"
          value={statusNames.APPROVED}
          badge={badgeValue}
          aria-label={getTabAriaLabel(entity, undefined)}
        ></Tabs.Tab>
        <Tabs.Tab
          title="Declined"
          value={statusNames.DECLINED}
          badge={badgeValue}
          aria-label={getTabAriaLabel(entity, undefined)}
        ></Tabs.Tab>
        <Tabs.Tab
          title="Deleted"
          value={statusNames.DELETED}
          badge={badgeValue}
          aria-label={getTabAriaLabel(entity, undefined)}
        ></Tabs.Tab>
      </Tabs>
      <Outlet />
    </>
  );

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

export default StatusTabs;
