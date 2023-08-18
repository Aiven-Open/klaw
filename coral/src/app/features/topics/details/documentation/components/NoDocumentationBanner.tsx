import { EmptyState, EmptyStateLayout } from "@aivenio/aquarium";
import illustration from "src/app/images/documentation-illustration.svg";

type NoDocumentationBannerProps = {
  addDocumentation: () => void;
  isUserTopicOwner: boolean;
};
function NoDocumentationBanner({
  addDocumentation,
  isUserTopicOwner,
}: NoDocumentationBannerProps) {
  if (!isUserTopicOwner) {
    return (
      <EmptyState
        title={"No readme available"}
        image={illustration}
        layout={EmptyStateLayout.CenterHorizontal}
      />
    );
  }

  return (
    <EmptyState
      title={"No readme available"}
      image={illustration}
      layout={EmptyStateLayout.CenterHorizontal}
      primaryAction={{
        text: "Add readme",
        onClick: addDocumentation,
      }}
    >
      Add a readme to give your team essential information, guidelines, and
      context about the topic.
    </EmptyState>
  );
}

export { NoDocumentationBanner };
