import { PageHeader } from "@aivenio/aquarium";
import { useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import ConnectorEditRequest from "src/app/features/connectors/request/ConnectorEditRequest";

const RequestConnectorEdit = () => {
  const navigate = useNavigate();
  const { connectorName } = useParams();

  useEffect(() => {
    if (connectorName === undefined) {
      navigate(-1);
    }
  }, []);

  return (
    <>
      <PreviewBanner
        linkTarget={`/connectorOverview?connectorName=${connectorName}`}
      />
      <PageHeader title={"Request connector update"} />
      <ConnectorEditRequest />
    </>
  );
};

export default RequestConnectorEdit;
