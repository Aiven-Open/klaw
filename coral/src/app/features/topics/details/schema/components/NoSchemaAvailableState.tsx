import { EmptyState, PageHeader } from "@aivenio/aquarium";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";
import { useNavigate } from "react-router-dom";

type NoSchemaAvailableStateProps = {
  topicName: string;
  isTopicOwner: boolean;
  topicSchemasIsRefetching: boolean;
  createSchemaAllowed: boolean;
};
function NoSchemaAvailableState({
  topicName,
  isTopicOwner,
  topicSchemasIsRefetching,
  createSchemaAllowed,
}: NoSchemaAvailableStateProps) {
  const navigate = useNavigate();

  if (!isTopicOwner) {
    return (
      <div data-testid={"no-schema-available-banner"}>
        <PageHeader title="Schema" />
        <EmptyState title="No schema available for this topic" />
      </div>
    );
  }

  return (
    <div data-testid={"no-schema-available-banner"}>
      <PageHeader title="Schema" />
      <EmptyState
        title="No schema available for this topic"
        primaryAction={{
          onClick: () => navigate(`/topic/${topicName}/request-schema`),
          text: "Request a new schema",
          disabled: topicSchemasIsRefetching || !createSchemaAllowed,
        }}
      >
        {!createSchemaAllowed && !topicSchemasIsRefetching && (
          <SchemaPromotableOnlyAlert />
        )}
      </EmptyState>
    </div>
  );
}

export { NoSchemaAvailableState };
