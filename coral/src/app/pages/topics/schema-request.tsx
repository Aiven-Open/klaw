import { PageHeader } from "@aivenio/aquarium";
import { useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";

const SchemaRequest = () => {
  const { topicName } = useParams();

  return (
    <>
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
    </>
  );
};

export default SchemaRequest;
