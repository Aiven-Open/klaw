import { PageHeader } from "@aivenio/aquarium";
import { useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicPromotionRequest from "src/app/features/topics/request/TopicPromotionRequest";

const RequestTopic = () => {
  const navigate = useNavigate();
  const { topicName } = useParams();

  useEffect(() => {
    if (topicName === undefined) {
      navigate(-1);
    }
  }, []);

  return (
    <>
      <PreviewBanner linkTarget={`/topicOverview?topicname=${topicName}`} />
      <PageHeader title={"Request topic promotion"} />
      <TopicPromotionRequest />
    </>
  );
};

export default RequestTopic;
