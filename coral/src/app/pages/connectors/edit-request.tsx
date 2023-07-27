import { PageHeader } from "@aivenio/aquarium";
import { useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";

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
      <div>Edit connector</div>
    </>
  );
};

export default RequestConnectorEdit;
