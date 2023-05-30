import ConnectorRequests from "src/app/features/requests/connectors/ConnectorRequests";
import PreviewBanner from "src/app/components/PreviewBanner";

const ConnectorRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/myConnectorRequests"} />
      <ConnectorRequests />
    </>
  );
};

export default ConnectorRequestsPage;
