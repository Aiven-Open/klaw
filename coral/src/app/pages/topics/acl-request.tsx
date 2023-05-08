import { PageHeader } from "@aivenio/aquarium";
import { useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import Layout from "src/app/layout/Layout";

const AclRequest = () => {
  const { topicName } = useParams();

  return (
    <Layout>
      <PreviewBanner
        linkTarget={`/requestAcls${
          topicName !== undefined ? `?topicName=${topicName}` : ""
        }`}
      />
      <PageHeader title={"ACL (Access Control) Request"} />
      <TopicAclRequest />
    </Layout>
  );
};

export default AclRequest;
