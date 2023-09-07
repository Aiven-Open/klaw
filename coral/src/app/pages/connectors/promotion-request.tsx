import { PageHeader } from "@aivenio/aquarium";
import { useNavigate, useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import { ConnectorPromotionRequest } from "src/app/features/connectors/request/ConnectorPromotionRequest";

const ConnectorPromotionRequestPage = () => {
  const navigate = useNavigate();
  const { connectorName } = useParams();

  if (connectorName === undefined) {
    navigate(-1);
    return <></>;
  }

  return (
    <>
      <PreviewBanner
        linkTarget={`/connectorOverview?connectorName=${connectorName}`}
      />
      <PageHeader title={"Request connector promotion"} />
      <ConnectorPromotionRequest />
    </>
  );
};

export { ConnectorPromotionRequestPage };
