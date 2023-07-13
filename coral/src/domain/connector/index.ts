import {
  approveConnectorRequest,
  createConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
  getConnectorOverview,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  getConnectors,
  updateConnectorDocumentation,
} from "src/domain/connector/connector-api";
import {
  Connector,
  ConnectorDocumentationMarkdown,
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
  updateConnectorDocumentation,
};

export type {
  Connector,
  ConnectorOverview,
  ConnectorRequest,
  ConnectorRequestsForApprover,
  CreateConnectorRequestPayload,
  ConnectorDocumentationMarkdown,
};
