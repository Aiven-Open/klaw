import { components } from "types/api";
import { ResolveIntersectionTypes } from "types/utils";

type RequestOperationType =
  | "CREATE"
  | "UPDATE"
  | "PROMOTE"
  | "CLAIM"
  | "DELETE";
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

type RequestsWaitingForApproval = {
  [key in RequestEntityType]: number;
};

export type {
  RequestOperationType,
  RequestStatus,
  RequestEntityType,
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestsWaitingForApproval,
};
