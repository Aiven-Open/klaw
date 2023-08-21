import { EmptyState, EmptyStateLayout } from "@aivenio/aquarium";
import illustration from "src/app/images/documentation-illustration.svg";

type NoDocumentationBannerProps = {
  entity: "connector" | "topic";
  addDocumentation: () => void;
  isUserOwner: boolean;
};
function NoDocumentationBanner({
  entity,
  addDocumentation,
  isUserOwner,
}: NoDocumentationBannerProps) {
  if (!isUserOwner) {
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
      {`Add a readme to give your team essential information, guidelines, and context about the ${entity}.`}
    </EmptyState>
  );
}

export { NoDocumentationBanner };
