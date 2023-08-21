import { EmptyState, PageHeader } from "@aivenio/aquarium";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";
import { useNavigate } from "react-router-dom";

type NoSchemaBannerProps = {
  topicName: string;
  isTopicOwner: boolean;
  schemaIsRefetching: boolean;
  isCreatingSchemaAllowed: boolean;
};
function NoSchemaBanner({
  topicName,
  isTopicOwner,
  schemaIsRefetching,
  isCreatingSchemaAllowed,
}: NoSchemaBannerProps) {
  const navigate = useNavigate();

  if (!isTopicOwner) {
    return <EmptyState title="No schema available for this topic" />;
  }

  return (
    <EmptyState
      title="No schema available for this topic"
      primaryAction={{
        onClick: () => navigate(`/topic/${topicName}/request-schema`),
        text: "Request a new schema",
        disabled: schemaIsRefetching || !isCreatingSchemaAllowed,
      }}
    >
      {!isCreatingSchemaAllowed && <SchemaPromotableOnlyAlert />}
    </EmptyState>
  );
}

export { NoSchemaBanner };
