import {
  getActivityLog,
  getRequestsStatistics,
  getRequestsWaitingForApproval,
} from "src/domain/requests/requests-api";
import {
  ActivityLog,
  RequestOperationType,
  RequestStatus,
  RequestEntityType,
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
  RequestsWaitingForApprovalWithTotal,
} from "src/domain/requests/requests-types";

export { getActivityLog, getRequestsStatistics, getRequestsWaitingForApproval };
export type {
  ActivityLog,
  RequestOperationType,
  RequestStatus,
  RequestEntityType,
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
  RequestsWaitingForApprovalWithTotal,
};
