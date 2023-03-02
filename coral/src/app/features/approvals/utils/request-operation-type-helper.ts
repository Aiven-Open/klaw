import { RequestOperationType } from "src/domain/requests/requests-types";
import { ChipStatus } from "@aivenio/aquarium";

// @TODO a nice improvement would be to
// have a more typesafe / exhaustive list
// here TS won't complain if a possible
// value is missing from the array.
const operationTypeList: RequestOperationType[] = [
  "CLAIM",
  "CREATE",
  "DELETE",
  "PROMOTE",
  "UPDATE",
];

const requestOperationTypeChipStatusMap: {
  [key in RequestOperationType]: ChipStatus;
} = {
  CLAIM: "neutral",
  CREATE: "neutral",
  DELETE: "neutral",
  PROMOTE: "neutral",
  UPDATE: "neutral",
};

const requestOperationTypeNameMap: {
  [key in RequestOperationType]: string;
} = {
  CLAIM: "Claim",
  CREATE: "Create",
  DELETE: "Delete",
  PROMOTE: "Promote",
  UPDATE: "Update",
};

export {
  operationTypeList,
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
};
