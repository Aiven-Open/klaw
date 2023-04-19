import PreviewBanner from "src/app/components/PreviewBanner";
import ConnectorApprovals from "src/app/features/approvals/connectors/ConnectorApprovals";

const ConnectorApprovalsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/execConnectors"} />
      <ConnectorApprovals />
    </>
  );
};

export default ConnectorApprovalsPage;
