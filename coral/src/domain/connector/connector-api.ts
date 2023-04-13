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
import api from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";
import { convertQueryValuesToString } from "src/services/api-helper";

const filterGetConnectorRequestParams = (
  params: KlawApiRequestQueryParameters<"getConnectorRequests">
) => {
  const isMyRequest = "true";
  // @TODO: update when this issue is resolved https://github.com/aiven/klaw/issues/974
  // const isMyRequest = String(Boolean(params.isMyRequest));

  return omitBy({ ...params, isMyRequest }, (value, property) => {
    const omitEnv = property === "env" && value === "ALL";
    const omitConnectorType = property === "aclType" && value === "ALL";
    const omitTopic = property === "topic" && value === "";
    const omitIsMyRequest = property === "isMyRequest" && value !== "true";
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
  });
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
      `/getConnectors?${new URLSearchParams(queryParams)}`
    )
    .then(transformConnectorApiResponse);
};

const getConnectorRequestsForApprover = (
  params: KlawApiRequestQueryParameters<"getCreatedConnectorRequests">
) => {
  const filteredParams = filterGetConnectorRequestParams(params);

  return api
    .get<KlawApiResponse<"getCreatedConnectorRequests">>(
      `/getConnectorRequestsForApprover?${new URLSearchParams(filteredParams)}`
    )
    .then(transformConnectorRequestApiResponse);
};

const getConnectorRequests = (
  params: KlawApiRequestQueryParameters<"getConnectorRequests">
) => {
  const filteredParams = filterGetConnectorRequestParams(params);

  return api
    .get<KlawApiResponse<"getConnectorRequests">>(
      `/getConnectorRequests?${new URLSearchParams(filteredParams)}`
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
  >(`/request/approve`, { requestEntityType: "CONNECTOR", reqIds });
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
  >(`/request/decline`, { requestEntityType: "CONNECTOR", reqIds, reason });
};

type DeleteConnectorRequestPayload = RequestVerdictDelete<"CONNECTOR">;
type DeleteRequestParams = {
  reqIds: DeleteConnectorRequestPayload["reqIds"];
};
const deleteConnectorRequest = ({ reqIds }: DeleteRequestParams) => {
  return api.post<
    KlawApiResponse<"deleteRequest">,
    DeleteConnectorRequestPayload
  >(`/request/delete`, { requestEntityType: "CONNECTOR", reqIds });
};

export {
  getConnectorRequestsForApprover,
  getConnectorRequests,
  approveConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectors,
};
