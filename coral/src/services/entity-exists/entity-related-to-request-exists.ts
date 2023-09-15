import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";

type DoesEntityExist = {
  requestStatus: RequestStatus;
  requestOperationType: RequestOperationType;
};
function doesEntityRelatedToRequestExists({
  requestStatus,
  requestOperationType,
}: DoesEntityExist): boolean {
  // a e.g. topic where a CREATE request has NOT been approved, does not exist as entity
  if (requestOperationType === "CREATE" && requestStatus !== "APPROVED") {
    return false;
  }

  // a e.g. topic where a DELETE request has been approved, does not exist as entity
  if (requestOperationType === "DELETE" && requestStatus === "APPROVED") {
    return false;
  }
  return true;
}

export { doesEntityRelatedToRequestExists };
