import Layout from "src/app/layout/Layout";
import { useNavigate, useParams } from "react-router-dom";
import { TopicOverview } from "src/app/features/topics/overview/TopicOverview";

function TopicOverviewPage() {
  const { topicName } = useParams();
  const navigate = useNavigate();

  if (!topicName) {
    navigate("/topics");
    return <></>;
  }

  return (
    <Layout>
      <TopicOverview topicName={topicName} />
    </Layout>
  );
}

export { TopicOverviewPage };
