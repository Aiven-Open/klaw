import { MarkdownString } from "src/domain/helper/documentation-helper";
import {
  KlawApiModel,
  KlawApiRequest,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";

type Connector = KlawApiModel<"KafkaConnectorModelResponse">;
type ConnectorApiResponse = ResolveIntersectionTypes<Paginated<Connector[]>>;

type ConnectorRequest = KlawApiModel<"KafkaConnectorRequestsResponseModel">;

type ConnectorDocumentationMarkdown = MarkdownString;

type ConnectorRequestsForApprover = ResolveIntersectionTypes<
  Paginated<ConnectorRequest[]>
>;

type CreateConnectorRequestPayload = KlawApiRequest<"createConnectorRequest">;

// KlawApiModel<"ConnectorOverview">
// Represents the ConnectorOverview as defined in the backend.
// "ConnectorOverview" is the type (and object) we're using in FE
// we're redefining property types here to fit our need in app better
// transformConnectorOverviewResponse() is taking care of transforming
// the properties and makes sure the types are matching between BE and FE
type ConnectorOverview = ResolveIntersectionTypes<
  Omit<KlawApiModel<"ConnectorOverview">, "connectorInfoList"> & {
    // "topicInfoList" is a list of KlawApiModel<"KafkaConnectorModelResponse">
    // there is only ever one entry in this list, and we want to access
    // it accordingly, that's why we transform it to connectorInfo instead
    connectorInfo: KlawApiModel<"KafkaConnectorModelResponse">;
    connectorDocumentation?: ConnectorDocumentationMarkdown;
  }
>;

export type {
  Connector,
  ConnectorApiResponse,
  ConnectorDocumentationMarkdown,
  ConnectorOverview,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
};
