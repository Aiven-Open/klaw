import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";
import { useParams } from "react-router-dom";

const SchemaRequest = () => {
  // @TODO: should we add verification that this is a real topic name?
  const { topicName } = useParams();

  return (
    <AuthenticationRequiredBoundary>
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
    </AuthenticationRequiredBoundary>
  );
};

export default SchemaRequest;
