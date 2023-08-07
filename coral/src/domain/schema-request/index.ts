import {
  approveSchemaRequest,
  requestSchemaCreation,
  declineSchemaRequest,
  getSchemaRequests,
  getSchemaRequestsForApprover,
  deleteSchemaRequest,
  requestSchemaPromotion,
} from "src/domain/schema-request/schema-request-api";
import {
  CreatedSchemaRequests,
  PromoteSchemaPayload,
  SchemaRequest,
  SchemaRequestApiResponse,
} from "src/domain/schema-request/schema-request-types";

export type {
  CreatedSchemaRequests,
  SchemaRequest,
  SchemaRequestApiResponse,
  PromoteSchemaPayload,
};
export {
  requestSchemaCreation,
  getSchemaRequestsForApprover,
  approveSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
  deleteSchemaRequest,
  requestSchemaPromotion,
};
