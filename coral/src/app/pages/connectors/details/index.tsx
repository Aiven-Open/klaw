import { Navigate, useParams } from "react-router-dom";
import { ConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";

function ConnectorDetailsPage() {
  const { connectorName } = useParams();
  

  if (!connectorName) {
    
    return <Navigate to="/connectors" replace={true}/>;
  }

  return <ConnectorDetails connectorName={connectorName} />;
}

export { ConnectorDetailsPage };
