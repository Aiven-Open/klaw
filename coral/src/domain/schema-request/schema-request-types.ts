import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";
import { RequestStatus, RequestType } from "src/domain/requests";

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

type SchemaRequestPayload = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Pick<KlawApiModel<"SchemaRequest">, "remarks" | "schemaversion" | "appname">
>;

type SchemaRequestType = RequestType;
type SchemaRequestStatus = RequestStatus;

type SchemaRequest = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      | "req_no"
      | "topicname"
      | "environment"
      | "environmentName"
      | "teamname"
      | "username"
      | "requesttimestring"
      | "requesttype"
      | "remarks"
      | "approvingTeamDetails"
    >
  > &
    KlawApiModel<"SchemaRequest">
>;

type SchemaRequestApiResponse = ResolveIntersectionTypes<
  Paginated<SchemaRequest[]>
>;

export type {
  SchemaRequestPayload,
  CreatedSchemaRequests,
  SchemaRequest,
  SchemaRequestType,
  SchemaRequestStatus,
  SchemaRequestApiResponse,
};
