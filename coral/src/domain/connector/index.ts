import {
  approveConnectorRequest,
  declineConnectorRequest,
  getConnectorRequests,
  getConnectorRequestsForApprover,
  deleteConnectorRequest,
} from "src/domain/connector/connector-api";
import {
  ConnectorRequest,
  ConnectorRequestsForApprover,
} from "src/domain/connector/connector-types";

export type { ConnectorRequestsForApprover, ConnectorRequest };
export {
  getConnectorRequestsForApprover,
  getConnectorRequests,
  approveConnectorRequest,
  declineConnectorRequest,
  deleteConnectorRequest,
};
