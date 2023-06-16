import {
  approveSchemaRequest,
  createSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
  getSchemaRequestsForApprover,
  deleteSchemaRequest,
  promoteSchemaRequest,
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
  createSchemaRequest,
  getSchemaRequestsForApprover,
  approveSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
  deleteSchemaRequest,
  promoteSchemaRequest,
};
