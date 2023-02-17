import {
  createSchemaRequest,
  getSchemaRequestsForApprover,
} from "src/domain/schema-request/schema-request-api";
import {
  CreatedSchemaRequests,
  SchemaRequest,
} from "src/domain/schema-request/schema-request-types";

export type { CreatedSchemaRequests, SchemaRequest };
export { createSchemaRequest, getSchemaRequestsForApprover };
