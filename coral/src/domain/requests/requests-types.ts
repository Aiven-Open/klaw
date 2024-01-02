import { components } from "types/api";
import { ResolveIntersectionTypes, KlawApiModel } from "types/utils";

type RequestOperationType =
  | "CREATE"
  | "UPDATE"
  | "PROMOTE"
  | "CLAIM"
  | "DELETE"
  | "ALL";
type RequestStatus = "CREATED" | "DELETED" | "DECLINED" | "APPROVED" | "ALL";
type RequestVerdict = components["schemas"]["RequestVerdict"];
type RequestEntityType =
  components["schemas"]["RequestVerdict"]["requestEntityType"];

type RequestVerdictApproval<T extends RequestEntityType> =
  ResolveIntersectionTypes<
    Omit<RequestVerdict, "reason"> & {
      requestEntityType: T;
    }
  >;

type RequestVerdictDecline<T extends RequestEntityType> =
  ResolveIntersectionTypes<
    Required<Pick<RequestVerdict, "reason">> &
      RequestVerdict & {
        requestEntityType: T;
      }
  >;

type RequestVerdictDelete<T extends RequestEntityType> =
  ResolveIntersectionTypes<
    Omit<RequestVerdict, "reason"> & {
      requestEntityType: T;
    }
  >;

type BaseRequestsWaitingForApproval = {
  [key in RequestEntityType]: number;
};

interface RequestsWaitingForApprovalWithTotal
  extends BaseRequestsWaitingForApproval {
  TOTAL_NOTIFICATIONS: number;
}

type ActivityLog = KlawApiModel<"ActivityLog">;

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
