import {
  approveConnectorRequest,
  declineConnectorRequest,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  deleteConnectorRequest,
  getConnectors,
  createConnectorRequest,
} from "src/domain/connector/connector-api";
import {
  ConnectorRequest,
  ConnectorRequestsForApprover,
  Connector,
  CreateConnectorRequestPayload,
} from "src/domain/connector/connector-types";

export type {
  Connector,
  ConnectorRequestsForApprover,
  ConnectorRequest,
  CreateConnectorRequestPayload,
};
export {
  getConnectorRequestsForApprover,
  getConnectorRequests,
  getConnectors,
  approveConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  createConnectorRequest,
};
