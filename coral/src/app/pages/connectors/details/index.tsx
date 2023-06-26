import { useNavigate, useParams } from "react-router-dom";
import { ConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";

function ConnectorDetailsPage() {
  const { connectorName } = useParams();
  const navigate = useNavigate();

  if (!connectorName) {
    navigate("/connectors");
    return <></>;
  }

  return <ConnectorDetails connectorName={connectorName} />;
}

export { ConnectorDetailsPage };
