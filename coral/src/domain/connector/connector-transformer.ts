import {
  ConnectorApiResponse,
  ConnectorOverview,
  ConnectorRequestsForApprover,
} from "src/domain/connector/connector-types";
import { KlawApiResponse } from "types/utils";
import omit from "lodash/omit";

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

function transformConnectorOverviewResponse(
  apiResponse: KlawApiResponse<"getConnectorOverview">
): ConnectorOverview {
  return {
    ...omit(apiResponse, ["connectorInfoList"]),
    connectorInfo: apiResponse.connectorInfoList[0],
  };
}

export {
  transformConnectorApiResponse,
  transformConnectorRequestApiResponse,
  transformConnectorOverviewResponse,
};
