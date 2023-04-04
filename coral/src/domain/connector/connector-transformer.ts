import { ConnectorRequestsForApprover } from "src/domain/connector/connector-types";
import { KlawApiResponse } from "types/utils";

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

export default transformConnectorRequestApiResponse;
