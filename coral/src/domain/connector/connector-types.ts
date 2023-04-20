import {
  KlawApiModel,
  KlawApiRequest,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";

type Connector = KlawApiModel<"KafkaConnectorModelResponse">;
type ConnectorApiResponse = ResolveIntersectionTypes<Paginated<Connector[]>>;

type ConnectorRequest = KlawApiModel<"KafkaConnectorRequestsResponseModel">;

type ConnectorRequestsForApprover = ResolveIntersectionTypes<
  Paginated<ConnectorRequest[]>
>;

type CreateConnectorRequestPayload = KlawApiRequest<"createConnectorRequest">;

export type {
  Connector,
  ConnectorApiResponse,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
};
