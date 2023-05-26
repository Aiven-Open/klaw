import AclRequests from "src/app/features/requests/acls/AclRequests";
import PreviewBanner from "src/app/components/PreviewBanner";

const AclRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/myAclRequests"} />
      <AclRequests />
    </>
  );
};

export default AclRequestsPage;
