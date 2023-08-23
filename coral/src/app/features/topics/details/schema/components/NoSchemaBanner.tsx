import { EmptyState } from "@aivenio/aquarium";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";
import { useNavigate } from "react-router-dom";
import { OpenSchemaRequestAlert } from "src/app/features/topics/details/schema/components/OpenSchemaRequestAlert";

type NoSchemaBannerProps = {
  topicName: string;
  isTopicOwner: boolean;
  schemaIsRefetching: boolean;
  isCreatingSchemaAllowed: boolean;
  hasOpenRequest: boolean;
};
function NoSchemaBanner({
  topicName,
  isTopicOwner,
  schemaIsRefetching,
  isCreatingSchemaAllowed,
  hasOpenRequest,
}: NoSchemaBannerProps) {
  const navigate = useNavigate();

  if (!isTopicOwner) {
    return <EmptyState title="No schema available for this topic" />;
  }

  if (hasOpenRequest) {
    return (
      <EmptyState
        title="No schema available for this topic"
        primaryAction={{
          onClick: () => navigate(`/topic/${topicName}/request-schema`),
          text: "Request a new schema",
          disabled: true,
        }}
      >
        <OpenSchemaRequestAlert topicName={topicName} />
      </EmptyState>
    );
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
