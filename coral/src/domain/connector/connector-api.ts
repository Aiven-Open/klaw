import omitBy from "lodash/omitBy";
import {
  transformConnectorApiResponse,
  transformConnectorOverviewResponse,
  transformConnectorRequestApiResponse,
} from "src/domain/connector/connector-transformer";
import {
  ConnectorClaimPayload,
  ConnectorDocumentationMarkdown,
  DeleteConnectorPayload,
} from "src/domain/connector/connector-types";
import { createStringifiedHtml } from "src/domain/helper/documentation-helper";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests";
import api, { API_PATHS, KlawApiError } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import {
  KlawApiModel,
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

const requestConnectorCreation = (
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

const requestConnectorEdit = (
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
    requestOperationType: "UPDATE",
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
    .then((connectorOverview) => {
      if (!connectorOverview.connectorExists) {
        // Currently the API returns a reduced TopicOverview when
        // a topic does not exist. In the future, it should return
        // a 404. Until then, we're doing that ourself.
        const connectorDoesNotExistError: KlawApiError = {
          message: "Connector does not exist",
          success: false,
        };
        throw connectorDoesNotExistError;
      }
      return transformConnectorOverviewResponse(connectorOverview);
    });
};

const getConnectorDetailsPerEnv = (
  params: KlawApiRequestQueryParameters<"getConnectorDetailsPerEnv">
) => {
  return api.get<KlawApiResponse<"getConnectorDetailsPerEnv">>(
    API_PATHS.getConnectorDetailsPerEnv,
    new URLSearchParams(params)
  );
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

async function requestConnectorDeletion({
  connectorName,
  envId,
}: DeleteConnectorPayload) {
  // We exclude 'remark' from the payload, as it is not yet implemented
  const payloadWithoutRemark = {
    connectorName,
    envId,
  };
  return api.post<
    KlawApiResponse<"createConnectorDeleteRequest">,
    KlawApiModel<"KafkaConnectorDeleteRequestModel">
  >(API_PATHS.createConnectorDeleteRequest, payloadWithoutRemark);
}

const requestConnectorClaim = (params: ConnectorClaimPayload) => {
  const payload: KlawApiModel<"ConnectorClaimRequestModel"> = {
    env: params.env,
    connectorName: params.connectorName,
  };

  return api.post<
    KlawApiResponse<"createClaimConnectorRequest">,
    KlawApiModel<"ConnectorClaimRequestModel">
  >(API_PATHS.createClaimConnectorRequest, payload);
};

const requestConnectorPromotion = (
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
    requestOperationType: "PROMOTE",
  });
};

export {
  approveConnectorRequest,
  requestConnectorCreation,
  declineConnectorRequest,
  requestConnectorDeletion,
  deleteConnectorRequest,
  requestConnectorEdit,
  getConnectorDetailsPerEnv,
  getConnectorOverview,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  getConnectors,
  updateConnectorDocumentation,
  requestConnectorClaim,
  requestConnectorPromotion,
};
