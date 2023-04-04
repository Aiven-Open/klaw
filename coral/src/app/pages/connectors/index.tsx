import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";

const ConnectorsPage = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PreviewBanner linkTarget={"/browseTopics"} />
        <PageHeader
          title={"All Kafka Connectors"}
          primaryAction={{
            text: "Request new Connector",
            onClick: () => (window.location.href = "/requestConnector"),
            icon: add,
          }}
        />
        <div>👋Kafka Connectors here </div>
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default ConnectorsPage;
