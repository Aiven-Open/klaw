import { EmptyState, EmptyStateLayout } from "@aivenio/aquarium";
import illustration from "src/app/images/topic-details-documentation-illustration.svg";

type NoDocumentationBannerProps = {
  addDocumentation: () => void;
};
function NoDocumentationBanner({
  addDocumentation,
}: NoDocumentationBannerProps) {
  return (
    <EmptyState
      title={"No documentation"}
      image={illustration}
      layout={EmptyStateLayout.CenterHorizontal}
      primaryAction={{
        text: "Add documentation",
        onClick: addDocumentation,
      }}
    >
      You can add documentation for your connector.
    </EmptyState>
  );
}

export { NoDocumentationBanner };
