import {
  approveConnectorRequest,
  declineConnectorRequest,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  deleteConnectorRequest,
  getConnectors,
} from "src/domain/connector/connector-api";
import {
  ConnectorRequest,
  ConnectorRequestsForApprover,
  Connector,
} from "src/domain/connector/connector-types";

export type { Connector, ConnectorRequestsForApprover, ConnectorRequest };
export {
  getConnectorRequestsForApprover,
  getConnectorRequests,
  getConnectors,
  approveConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
};
