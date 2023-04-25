import isString from "lodash/isString";
import { Environment } from "src/domain/environment";

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

function generateNamePatternString(patternList: string[]) {
  if (patternList.length === 1) return `"${patternList[0]}"`;
  return patternList
    .map((entry, index) => {
      if (
        (patternList.length === 2 && index === 0) ||
        index === patternList.length - 2
      ) {
        return `"${entry}" or `;
      }
      if (
        (patternList.length === 2 && index === 1) ||
        index === patternList.length - 1
      ) {
        return `"${entry}"`;
      }
      return `"${entry}", `;
    })
    .join("");
}
function generateTopicNameDescription(
  environmentParams: Environment["params"] | undefined
) {
  const desc: string[] = [
    "Allowed characters: letter, digit, period, underscore, and hyphen.",
  ];

  if (environmentParams) {
    const { applyRegex, topicRegex, topicPrefix, topicSuffix } =
      environmentParams;

    if (applyRegex && topicRegex && topicRegex.length > 0) {
      desc.unshift(
        `Follow name pattern: ${generateNamePatternString(topicRegex)}.`
      );
    }

    if (topicPrefix && topicPrefix.length > 0) {
      desc.unshift(
        `Prefix name with: ${generateNamePatternString(topicPrefix)}.`
      );
    }

    if (topicSuffix && topicSuffix.length > 0) {
      desc.unshift(
        `Suffix name with: ${generateNamePatternString(topicSuffix)}.`
      );
    }
  }
  return desc.join(" ");
}

export {
  transformAdvancedConfigEntries,
  generateTopicNameDescription,
  generateNamePatternString,
};
