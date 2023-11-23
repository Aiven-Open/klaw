import { useNavigate, useParams } from "react-router-dom";
import { ConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import PreviewBanner from "src/app/components/PreviewBanner";

function ConnectorDetailsPage() {
  const { connectorName } = useParams();
  const navigate = useNavigate();

  if (!connectorName) {
    navigate("/connectors");
    return <></>;
  }

  return (
    <>
      <PreviewBanner
        linkTarget={`/connectorOverview?connectorName=${connectorName}`}
      />
      <ConnectorDetails connectorName={connectorName} />
    </>
  );
}

export { ConnectorDetailsPage };
