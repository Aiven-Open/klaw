import Layout from "src/app/layout/Layout";
import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { Flexbox, PageHeader } from "@aivenio/design-system";

const Topics = () => {
  return (
    <Layout>
      <Flexbox direction={"column"} rowGap={"l2"}>
        <PageHeader title={"Browse all topics"} />

        <BrowseTopics />
      </Flexbox>
    </Layout>
  );
};

export default Topics;
