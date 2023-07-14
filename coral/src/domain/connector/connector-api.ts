import omitBy from "lodash/omitBy";
import {
  transformConnectorApiResponse,
  transformConnectorOverviewResponse,
  transformConnectorRequestApiResponse,
} from "src/domain/connector/connector-transformer";
import { ConnectorDocumentationMarkdown } from "src/domain/connector/connector-types";
import { createStringifiedHtml } from "src/domain/helper/documentation-helper";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import api, { API_PATHS } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";

const filterGetConnectorRequestParams = (
  params: KlawApiRequestQueryParameters<"getConnectorRequests">
) => {
  return omitBy(
    { ...params, isMyRequest: String(Boolean(params.isMyRequest)) },
    (value, property) => {
      const omitEnv = property === "env" && value === "ALL";
      const omitConnectorType = property === "aclType" && value === "ALL";
      const omitTopic = property === "topic" && value === "";
      const omitIsMyRequest = property === "isMyRequest" && value !== "true"; // Omit if anything else than true
      const omitOperationType =
        property === "operationType" && value === undefined;
      const omitSearch =
        property === "search" && (value === "" || value === undefined);

      return (
        omitEnv ||
        omitConnectorType ||
        omitTopic ||
        omitOperationType ||
        omitIsMyRequest ||
        omitSearch
      );
    }
  );
};

const getConnectors = (
  params: KlawApiRequestQueryParameters<"getConnectors">
) => {
  const queryParams = convertQueryValuesToString({
    env: params.env,
    pageNo: params.pageNo,
    ...(params.teamId && { teamId: params.teamId }),
    ...(params.connectornamesearch &&
      params.connectornamesearch.length > 0 && {
        connectornamesearch: params.connectornamesearch,
      }),
  });

  return api
    .get<KlawApiResponse<"getConnectors">>(
      API_PATHS.getConnectors,
      new URLSearchParams(queryParams)
    )
    .then(transformConnectorApiResponse);
};

const getConnectorRequestsForApprover = (
  params: KlawApiRequestQueryParameters<"getCreatedConnectorRequests">
) => {
  const filteredParams = filterGetConnectorRequestParams(params);

  return api
    .get<KlawApiResponse<"getCreatedConnectorRequests">>(
      API_PATHS.getCreatedConnectorRequests,
      new URLSearchParams(filteredParams)
    )
    .then(transformConnectorRequestApiResponse);
};

const getConnectorRequests = (
  params: KlawApiRequestQueryParameters<"getConnectorRequests">
) => {
  const filteredParams = filterGetConnectorRequestParams(params);

  return api
    .get<KlawApiResponse<"getConnectorRequests">>(
      API_PATHS.getConnectorRequests,
      new URLSearchParams(filteredParams)
    )
    .then(transformConnectorRequestApiResponse);
};

type ApproveConnectorRequestPayload = RequestVerdictApproval<"CONNECTOR">;
type ApproveRequestParams = {
  reqIds: ApproveConnectorRequestPayload["reqIds"];
};
const approveConnectorRequest = ({ reqIds }: ApproveRequestParams) => {
  return api.post<
    KlawApiResponse<"approveRequest">,
    ApproveConnectorRequestPayload
  >(API_PATHS.approveRequest, { requestEntityType: "CONNECTOR", reqIds });
};

type DeclineConnectorRequestPayload = RequestVerdictDecline<"CONNECTOR">;
type DeclineRequestParams = {
  reqIds: DeclineConnectorRequestPayload["reqIds"];
  reason: DeclineConnectorRequestPayload["reason"];
};
const declineConnectorRequest = ({ reqIds, reason }: DeclineRequestParams) => {
  return api.post<
    KlawApiResponse<"declineRequest">,
    DeclineConnectorRequestPayload
  >(API_PATHS.declineRequest, {
    requestEntityType: "CONNECTOR",
    reqIds,
    reason,
  });
};

type DeleteConnectorRequestPayload = RequestVerdictDelete<"CONNECTOR">;
type DeleteRequestParams = {
  reqIds: DeleteConnectorRequestPayload["reqIds"];
};
const deleteConnectorRequest = ({ reqIds }: DeleteRequestParams) => {
  return api.post<
    KlawApiResponse<"deleteRequest">,
    DeleteConnectorRequestPayload
  >(API_PATHS.deleteRequest, { requestEntityType: "CONNECTOR", reqIds });
};

const createConnectorRequest = (
  connectorPayload: Omit<
    KlawApiRequest<"createConnectorRequest">,
    "requestOperationType"
  >
) => {
  return api.post<
    KlawApiResponse<"createConnectorRequest">,
    KlawApiRequest<"createConnectorRequest">
  >(API_PATHS.createConnectorRequest, {
    ...connectorPayload,
    requestOperationType: "CREATE",
  });
};

type GetConnectorOverviewParams =
  KlawApiRequestQueryParameters<"getConnectorOverview">;
const getConnectorOverview = ({
  connectornamesearch,
  environmentId,
}: GetConnectorOverviewParams) => {
  const queryParams = convertQueryValuesToString({
    connectornamesearch,
    ...(environmentId && { environmentId }),
  });

  return api
    .get<KlawApiResponse<"getConnectorOverview">>(
      API_PATHS.getConnectorOverview,
      new URLSearchParams(queryParams)
    )
    .then(transformConnectorOverviewResponse);
};

type UpdateConnectorDocumentation = {
  connectorName: string;
  connectorIdForDocumentation: number;
  connectorDocumentation: ConnectorDocumentationMarkdown;
};
async function updateConnectorDocumentation({
  connectorName,
  connectorIdForDocumentation,
  connectorDocumentation,
}: UpdateConnectorDocumentation) {
  const stringifiedHtml = await createStringifiedHtml(connectorDocumentation);

  return api.post<
    KlawApiResponse<"saveConnectorDocumentation">,
    KlawApiRequest<"saveConnectorDocumentation">
  >(API_PATHS.saveConnectorDocumentation, {
    connectorName,
    connectorId: connectorIdForDocumentation,
    documentation: stringifiedHtml,
  });
}

export {
  approveConnectorRequest,
  createConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectorOverview,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  getConnectors,
  updateConnectorDocumentation,
};
