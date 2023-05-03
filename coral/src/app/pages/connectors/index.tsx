import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";

const ConnectorsPage = () => {
  const navigate = useNavigate();

  return (
    <AuthenticationRequiredBoundary>
      <>
        <PreviewBanner linkTarget={"/kafkaConnectors"} />
        <PageHeader
          title={"All Kafka Connectors"}
          primaryAction={{
            text: "Request new Connector",
            onClick: () => navigate("/connectors/request"),
            icon: add,
          }}
        />
        <BrowseConnectors />
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default ConnectorsPage;
