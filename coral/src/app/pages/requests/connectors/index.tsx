import { ConnectorRequests } from "src/app/features/requests/connectors/ConnectorRequests";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";

const ConnectorRequestsPage = () => {
  return (
    <Layout>
      <PreviewBanner linkTarget={"/myConnectorRequests"} />
      <ConnectorRequests />
    </Layout>
  );
};

export default ConnectorRequestsPage;
