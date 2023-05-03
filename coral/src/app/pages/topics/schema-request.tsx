import { PageHeader } from "@aivenio/aquarium";
import { useParams } from "react-router-dom";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";

const SchemaRequest = () => {
  // @TODO: should we add verification that this is a real topic name?
  const { topicName } = useParams();

  return (
    <AuthenticationRequiredBoundary>
      <>
        {topicName && (
          <>
            <PreviewBanner
              linkTarget={`/requestSchema?topicname=${topicName}`}
            />
            <PageHeader title={`Request new schema for topic "${topicName}"`} />
            <TopicSchemaRequest topicName={topicName} />
          </>
        )}
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default SchemaRequest;
