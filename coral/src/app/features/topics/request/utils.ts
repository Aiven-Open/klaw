import isString from "lodash/isString";
import { KlawApiRequest } from "types/utils";
import { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";

function createTopicRequestPayload(
  formData: Schema
): KlawApiRequest<"createTopicsCreateRequest"> {
  return {
    description: formData.description,
    environment: formData.environment.id,
    remarks: formData.remarks,
    topicname: formData.topicname,
    replicationfactor: formData.replicationfactor,
    topicpartitions: parseInt(formData.topicpartitions, 10),
    advancedTopicConfigEntries: transformAdvancedConfigEntries(
      formData.advancedConfiguration
    ),
    requestOperationType: "CREATE",
  };
}
function coerceToString(value: unknown): string | undefined {
  switch (typeof value) {
    case "string":
      return value;
    case "boolean":
    case "number":
      return value.toString();
    default:
      return undefined;
  }
}

function transformAdvancedConfigEntries(
  formData: string | undefined
): { configKey: string; configValue: string }[] {
  if (formData === undefined) {
    return [];
  }
  // Add some try catches, but monaco should take care of this
  const asObject = JSON.parse(formData);
  return Object.entries(asObject)
    .map(([key, value]) => ({ configKey: key, configValue: value }))
    .reduce((acc, { configKey, configValue }) => {
      const valueAsString = coerceToString(configValue);
      if (isString(valueAsString)) {
        return acc.concat([{ configKey, configValue: valueAsString }]);
      }
      return acc;
    }, [] as { configKey: string; configValue: string }[]);
}

export { transformAdvancedConfigEntries, createTopicRequestPayload };
