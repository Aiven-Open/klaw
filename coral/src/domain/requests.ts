import { components } from "types/api";
import { ResolveIntersectionTypes } from "types/utils";
type RequestOperationType = components["schemas"]["RequestOperationType"];
type RequestStatus = components["schemas"]["RequestStatus"];
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

export type {
  RequestOperationType,
  RequestStatus,
  RequestEntityType,
  RequestVerdictApproval,
  RequestVerdictDecline,
};
