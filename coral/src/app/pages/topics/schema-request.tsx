import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";
import { useParams } from "react-router-dom";

const SchemaRequest = () => {
  // @TODO: should we add verification that this is a real topic name?
  const { topicName } = useParams();

  return (
    <Layout>
      <PreviewBanner
        linkTarget={
          topicName === undefined
            ? `/requestSchema`
            : `/requestSchema?topicname=${topicName}`
        }
      />
      <PageHeader
        title={
          topicName === undefined
            ? `Request new schema`
            : `Request new schema for topic "${topicName}"`
        }
      />
      <TopicSchemaRequest topicName={topicName} />
    </Layout>
  );
};

export default SchemaRequest;
