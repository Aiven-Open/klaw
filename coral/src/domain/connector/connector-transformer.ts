import {
  ConnectorApiResponse,
  ConnectorRequestsForApprover,
} from "src/domain/connector/connector-types";
import { KlawApiResponse } from "types/utils";

const transformConnectorApiResponse = (
  data: KlawApiResponse<"getConnectors">
): ConnectorApiResponse => {
  if (data.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }
  return {
    totalPages: Number(data[0][0].totalNoPages),
    currentPage: Number(data[0][0].currentPage),
    entries: data.flat(),
  };
};

const transformConnectorRequestApiResponse = (
  data: KlawApiResponse<"getCreatedConnectorRequests">
): ConnectorRequestsForApprover => {
  if (data.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }
  return {
    totalPages: Number(data[0].totalNoPages),
    currentPage: Number(data[0].currentPage),
    entries: data,
  };
};

export { transformConnectorApiResponse, transformConnectorRequestApiResponse };
