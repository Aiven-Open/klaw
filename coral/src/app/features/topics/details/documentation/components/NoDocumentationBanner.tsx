import { EmptyState, EmptyStateLayout } from "@aivenio/aquarium";
import illustration from "src/app/images/topic-details-documentation-illustration.svg";

function NoDocumentationBanner() {
  return (
    <EmptyState
      title={"No documentation"}
      image={illustration}
      layout={EmptyStateLayout.CenterHorizontal}
      primaryAction={{
        text: "Add documentation",
        onClick: () => console.log("hu"),
      }}
    >
      You can add documentation for your topic.
    </EmptyState>
  );
}

export { NoDocumentationBanner };
