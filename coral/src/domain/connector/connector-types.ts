import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type ConnectorRequest = KlawApiModel<"KafkaConnectorRequestsResponseModel">;

type ConnectorRequestsForApprover = ResolveIntersectionTypes<
  Paginated<ConnectorRequest[]>
>;

export type { ConnectorRequest, ConnectorRequestsForApprover };
