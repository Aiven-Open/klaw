import PreviewBanner from "src/app/components/PreviewBanner";
import AclApprovals from "src/app/features/approvals/acls/AclApprovals";

const AclApprovalsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/execAcls"} />
      <AclApprovals />
    </>
  );
};

export default AclApprovalsPage;
