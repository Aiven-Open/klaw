import {
  ConnectorApiResponse,
  ConnectorOverview,
  ConnectorRequestsForApprover,
} from "src/domain/connector/connector-types";
import { KlawApiResponse } from "types/utils";
import omit from "lodash/omit";
import { createMarkdown } from "src/domain/helper/documentation-helper";

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

async function transformConnectorOverviewResponse(
  apiResponse: KlawApiResponse<"getConnectorOverview">
): Promise<ConnectorOverview> {
  // while we save documentation in stringified html to be
  // backwards compatible with the Angular app for now,
  // we're planing to migrate to pure Markdown at some
  // point and don't want stringified html to bleed into
  // an area outside from `/domain`

  let documentation;
  if (
    apiResponse.connectorDocumentation &&
    apiResponse.connectorDocumentation.trim().length > 0
  ) {
    documentation = await createMarkdown(apiResponse.connectorDocumentation);
  }
  return {
    ...omit(apiResponse, ["connectorInfoList"]),
    connectorInfo: apiResponse.connectorInfoList[0],
    connectorDocumentation: documentation,
  };
}

export {
  transformConnectorApiResponse,
  transformConnectorRequestApiResponse,
  transformConnectorOverviewResponse,
};
