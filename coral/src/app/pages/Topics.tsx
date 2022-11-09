import Layout from "src/app/layout/Layout";
import { BrowseTopics } from "src/app/features/topics/BrowseTopics";
import { Typography } from "@aivenio/design-system";

const Topics = () => {
  return (
    <Layout>
      <>
        <Typography.Heading htmlTag={"h1"}>
          Browse all topics
        </Typography.Heading>
        <div style={{ paddingTop: "30px" }}>
          <BrowseTopics />
        </div>
      </>
    </Layout>
  );
};

export default Topics;
