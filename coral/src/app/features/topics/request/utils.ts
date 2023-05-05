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

    // if a topic name has a regex format, it can't have a
    // prefix or suffix
    if (applyRegex) {
      if (topicRegex && topicRegex.length > 0) {
        desc.unshift(
          `Follow name pattern: ${generateNamePatternString(topicRegex)}.`
        );
      }
    } else {
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
  }
  return desc.join(" ");
}

function stripPrefixAndSuffix({
  topicName,
  prefixOrPrefix,
}: {
  topicName: string;
  prefixOrPrefix: string[];
}): string {
  // sorting first prevents to remove pre/suffixes that are
  // partial matches to others, e.g. remove "team_" when another
  // prefix is "devteam_, which would not be found and removed then
  return prefixOrPrefix
    .sort((a, b) => b.length - a.length)
    .reduce((acc: string, cur: string) => {
      return acc.split(cur).filter(Boolean).join("");
    }, topicName);
}

function isValidTopicNameWithPrefix({
  topicName,
  prefix,
  defaultPattern,
}: {
  topicName: string;
  prefix: string[];
  defaultPattern: RegExp;
}): boolean {
  if (prefix.length === 0) {
    return true; // No prefixes to match, so it always passes
  }

  const prefixMatch = prefix.some((p) => topicName.startsWith(p));
  const topicNameWithoutPrefix = stripPrefixAndSuffix({
    topicName: topicName,
    prefixOrPrefix: prefix,
  });

  return (
    prefixMatch &&
    topicNameWithoutPrefix.length >= 3 &&
    defaultPattern.test(topicNameWithoutPrefix)
  );
}

function isValidTopicNameWithSuffix({
  topicName,
  suffix,
  defaultPattern,
}: {
  topicName: string;
  suffix: string[];
  defaultPattern: RegExp;
}): boolean {
  if (suffix.length === 0) {
    return true; // No suffixes to match, so it always passes
  }

  const suffixMatch = suffix.some((s) => topicName.endsWith(s));
  const topicNameWithoutSuffix = stripPrefixAndSuffix({
    topicName: topicName,
    prefixOrPrefix: suffix,
  });

  return (
    suffixMatch &&
    topicNameWithoutSuffix.length >= 3 &&
    defaultPattern.test(topicNameWithoutSuffix)
  );
}

function isValidTopicNameWithPrefixAndSuffix({
  topicName,
  prefix,
  suffix,
  defaultPattern,
}: {
  topicName: string;
  prefix: string[];
  suffix: string[];
  defaultPattern: RegExp;
}): boolean {
  if (prefix.length === 0 && suffix.length === 0) {
    return true;
  }

  const prefixValid = isValidTopicNameWithPrefix({
    topicName: topicName,
    prefix,
    defaultPattern,
  });

  const suffixValid = isValidTopicNameWithSuffix({
    topicName: topicName,
    suffix,
    defaultPattern,
  });

  const topicNameStripped = stripPrefixAndSuffix({
    topicName: topicName,
    prefixOrPrefix: [...suffix, ...prefix],
  });

  return prefixValid && suffixValid && topicNameStripped.length > 2;
}

export {
  transformAdvancedConfigEntries,
  generateTopicNameDescription,
  generateNamePatternString,
  isValidTopicNameWithPrefix,
  isValidTopicNameWithSuffix,
  isValidTopicNameWithPrefixAndSuffix,
};
