import Layout from "src/app/layout/Layout";
import { BrowseTopics } from "src/app/features/topics/BrowseTopics";

const Topics = () => {
  return (
    <Layout>
      <>
        <h1>Topics</h1>
        <BrowseTopics />
      </>
    </Layout>
  );
};

export default Topics;
