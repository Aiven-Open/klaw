import { PageHeader } from "@aivenio/aquarium";
import { useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";

const AclRequest = () => {
  const { topicName } = useParams();

  return (
    <>
      <PreviewBanner
        linkTarget={`/requestAcls${
          topicName !== undefined ? `?topicName=${topicName}` : ""
        }`}
      />
      <PageHeader title={"ACL (Access Control) request"} />
      <TopicAclRequest />
    </>
  );
};

export default AclRequest;
