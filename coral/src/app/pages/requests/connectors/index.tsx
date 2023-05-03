import { ConnectorRequests } from "src/app/features/requests/connectors/ConnectorRequests";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";

const ConnectorRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/myConnectorRequests"} />
      <ConnectorRequests />
    </>
  );
};

export default ConnectorRequestsPage;
