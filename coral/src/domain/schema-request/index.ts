import {
  approveSchemaRequest,
  createSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
  getSchemaRequestsForApprover,
  deleteSchemaRequest,
} from "src/domain/schema-request/schema-request-api";
import {
  CreatedSchemaRequests,
  SchemaRequest,
} from "src/domain/schema-request/schema-request-types";

export type { CreatedSchemaRequests, SchemaRequest };
export {
  createSchemaRequest,
  getSchemaRequestsForApprover,
  getSchemaRequests,
  approveSchemaRequest,
  declineSchemaRequest,
  deleteSchemaRequest,
};
