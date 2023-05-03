import { PageHeader } from "@aivenio/aquarium";
import { useParams } from "react-router-dom";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";

const AclRequest = () => {
  const { topicName } = useParams();

  return (
    <AuthenticationRequiredBoundary>
      <>
        <PreviewBanner
          linkTarget={`/requestAcls${
            topicName !== undefined ? `?topicName=${topicName}` : ""
          }`}
        />
        <PageHeader title={"ACL (Access Control) Request"} />
        <TopicAclRequest />
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default AclRequest;
