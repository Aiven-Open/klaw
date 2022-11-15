import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { Flexbox, PageHeader } from "@aivenio/design-system";

const Topics = () => {
  return (
    <Flexbox direction={"column"} rowGap={"l2"}>
      <PageHeader title={"Browse all topics"} />

      <BrowseTopics />
    </Flexbox>
  );
};

export default Topics;
