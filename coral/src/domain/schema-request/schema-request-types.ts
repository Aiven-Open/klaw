import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";
import { RequestStatus, RequestOperationType } from "src/domain/requests";

type CreatedSchemaRequests = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      | "req_no"
      | "topicname"
      | "environmentName"
      | "username"
      | "requesttimestring"
    >
  >
>;

type CreateSchemaRequestPayload = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Partial<KlawApiModel<"SchemaRequest">>
>;

type SchemaRequestOperationType = RequestOperationType;
type SchemaRequestStatus = RequestStatus;

type SchemaRequest = ResolveIntersectionTypes<KlawApiModel<"SchemaRequest">>;

type SchemaRequestApiResponse = ResolveIntersectionTypes<
  Paginated<SchemaRequest[]>
>;

export type {
  CreateSchemaRequestPayload,
  CreatedSchemaRequests,
  SchemaRequest,
  SchemaRequestOperationType,
  SchemaRequestStatus,
  SchemaRequestApiResponse,
};
