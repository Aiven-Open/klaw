import Layout from "src/app/layout/Layout";
import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { PageHeader } from "@aivenio/design-system";

const Topics = () => {
  return (
    <Layout>
      <>
        <PageHeader title={"Browse all topics"} />
        <div style={{ paddingTop: "30px" }}>
          <BrowseTopics />
        </div>
      </>
    </Layout>
  );
};

export default Topics;
