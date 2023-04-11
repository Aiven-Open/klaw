import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type Connector = KlawApiModel<"KafkaConnectorModelResponse">;
type ConnectorApiResponse = ResolveIntersectionTypes<Paginated<Connector[]>>;

type ConnectorRequest = KlawApiModel<"KafkaConnectorRequestsResponseModel">;

type ConnectorRequestsForApprover = ResolveIntersectionTypes<
  Paginated<ConnectorRequest[]>
>;

export type {
  Connector,
  ConnectorApiResponse,
  ConnectorRequest,
  ConnectorRequestsForApprover,
};
