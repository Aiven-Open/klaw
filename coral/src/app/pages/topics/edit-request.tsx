import { PageHeader } from "@aivenio/aquarium";
import { useNavigate, useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicEditRequest from "src/app/features/topics/request/TopicEditRequest";

const RequestTopicEdit = () => {
  const navigate = useNavigate();
  const { topicName } = useParams();

  if (topicName === undefined) {
    navigate(-1);
    return <></>;
  }

  return (
    <>
      <PreviewBanner linkTarget={`/topicOverview?topicname=${topicName}`} />
      <PageHeader title={"Request topic update"} />
      <TopicEditRequest />
    </>
  );
};

export default RequestTopicEdit;
