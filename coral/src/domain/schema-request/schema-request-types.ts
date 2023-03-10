import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";
import {
  RequestStatus,
  RequestOperationType,
} from "src/domain/requests/requests-types";
import { operations } from "types/api";

type CreatedSchemaRequests = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequestModel">,
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
      KlawApiModel<"SchemaRequestModel">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Partial<KlawApiModel<"SchemaRequestModel">>
>;

type SchemaRequestOperationType = RequestOperationType;
type SchemaRequestStatus = RequestStatus;

type SchemaRequest = ResolveIntersectionTypes<
  KlawApiModel<"SchemaRequestModel">
>;

type SchemaRequestApiResponse = ResolveIntersectionTypes<
  Paginated<SchemaRequest[]>
>;

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Required<
    Pick<
      operations["getSchemaRequestsForApprover"]["parameters"]["query"],
      "pageNo" | "requestStatus"
    >
  >
>;

export type {
  CreateSchemaRequestPayload,
  CreatedSchemaRequests,
  SchemaRequest,
  SchemaRequestOperationType,
  SchemaRequestStatus,
  SchemaRequestApiResponse,
  GetSchemaRequestsQueryParams,
};
