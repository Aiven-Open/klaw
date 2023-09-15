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

/**
 * "remark" is currently not implemented in the API
 * and will be added later. We're already preparing
 * our UI and code for that.
 **/
type DeleteConnectorPayload = ResolveIntersectionTypes<
  KlawApiModel<"KafkaConnectorDeleteRequestModel"> & {
    remark?: string;
  }
>;

type ConnectorDetailsForEnv = KlawApiModel<"ConnectorOverviewPerEnv">;

// "remark" is currently not implemented in the API
// and will be added later. We're already preparing
// our UI and code for that.
type ConnectorClaimPayload = ResolveIntersectionTypes<
  KlawApiModel<"ConnectorClaimRequestModel"> & {
    remark?: string;
  }
>;

export type {
  Connector,
  ConnectorApiResponse,
  ConnectorDetailsForEnv,
  ConnectorDocumentationMarkdown,
  ConnectorOverview,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
  DeleteConnectorPayload,
  ConnectorClaimPayload,
};
