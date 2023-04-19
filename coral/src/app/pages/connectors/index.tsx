import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";

const ConnectorsPage = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PreviewBanner linkTarget={"/kafkaConnectors"} />
        <PageHeader
          title={"All Kafka Connectors"}
          primaryAction={{
            text: "Request new Connector",
            onClick: () => (window.location.href = "/requestConnector"),
            icon: add,
          }}
        />
        <BrowseConnectors />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default ConnectorsPage;
