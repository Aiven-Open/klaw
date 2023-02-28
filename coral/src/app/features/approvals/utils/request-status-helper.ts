import { RequestStatus } from "src/domain/requests";
import { ChipStatus } from "@aivenio/aquarium";

// @TODO a nice improvement would be to
// have a more typesafe / exhaustive list
// here TS won't complain if a possible
// value is missing from the array.
const statusList: RequestStatus[] = [
  "ALL",
  "APPROVED",
  "CREATED",
  "DECLINED",
  "DELETED",
];

const requestStatusChipStatusMap: { [key in RequestStatus]: ChipStatus } = {
  ALL: "neutral",
  APPROVED: "success",
  CREATED: "info",
  DECLINED: "warning",
  DELETED: "danger",
};

const requestStatusNameMap: {
  [key in RequestStatus]: string;
} = {
  ALL: "All statuses",
  APPROVED: "Approved",
  CREATED: "Created",
  DECLINED: "Declined",
  DELETED: "Deleted",
};

export { requestStatusNameMap, requestStatusChipStatusMap, statusList };
