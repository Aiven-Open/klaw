import { RequestStatus } from "src/domain/requests/requests-types";
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

enum statusNames {
  "ALL" = "ALL",
  "APPROVED" = "APPROVED",
  "CREATED" = "CREATED",
  "DECLINED" = "DECLINED",
  "DELETED" = "DELETED",
}

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
  CREATED: "Awaiting approval",
  DECLINED: "Declined",
  DELETED: "Deleted",
};

// This is a typeguard, explicit any is the correct type
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const isStatusName = (name: any): name is RequestStatus => {
  return statusList.includes(name);
};

export {
  requestStatusNameMap,
  requestStatusChipStatusMap,
  statusList,
  statusNames,
  isStatusName,
};
