import omitBy from "lodash/omitBy";
import {
  transformConnectorRequestApiResponse,
  transformConnectorApiResponse,
} from "src/domain/connector/connector-transformer";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import api, { API_PATHS } from "src/services/api";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";
import { convertQueryValuesToString } from "src/services/api-helper";

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

export {
  getConnectorRequestsForApprover,
  getConnectorRequests,
  approveConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectors,
  createConnectorRequest,
};
