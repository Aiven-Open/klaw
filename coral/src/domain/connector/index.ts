import {
  approveConnectorRequest,
  createConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectorOverview,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  getConnectors,
} from "src/domain/connector/connector-api";
import {
  Connector,
  ConnectorOverview,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
} from "src/domain/connector/connector-types";

export {
  approveConnectorRequest,
  createConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectorOverview,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  getConnectors,
};

export type {
  Connector,
  ConnectorOverview,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
};
