import BrowseTopics from "src/app/features/browse-topics/BrowseTopics";
import { Flexbox, PageHeader } from "@aivenio/design-system";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";

const Topics = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Flexbox direction={"column"} rowGap={"l2"}>
        <PageHeader title={"Browse all topics"} />
        <BrowseTopics />
      </Flexbox>
    </AuthenticationRequiredBoundary>
  );
};

export default Topics;
