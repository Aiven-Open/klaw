import { checkExhaustive } from "src/services/check-exhaustive";
import { RequestStatus } from "src/domain/requests";
import { ChipStatus } from "@aivenio/aquarium";

function getRequestStatusName(status: RequestStatus): string {
  switch (status) {
    case "ALL":
      return "All statuses";
    case "APPROVED":
      return "Approved";
    case "CREATED":
      return "Created";
    case "DECLINED":
      return "Declined";
    case "DELETED":
      return "Deleted";
  }
  checkExhaustive(status);
}

function getRequestStatusColor(status: RequestStatus): ChipStatus {
  switch (status) {
    case "ALL":
      return "neutral";
    case "APPROVED":
      return "success";
    case "CREATED":
      return "info";
    case "DECLINED":
      return "warning";
    case "DELETED":
      return "danger";
  }
  checkExhaustive(status);
}

export { getRequestStatusColor, getRequestStatusName };
