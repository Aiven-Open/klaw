import {
  generateNamePatternString,
  generateTopicNameDescription,
  transformAdvancedConfigEntries,
} from "src/app/features/topics/request/utils";
import { Environment } from "src/domain/environment";

describe("TopicRequest utils", () => {
  describe("transformAdvancedConfigEntries", () => {
    it("transforms empty object into empty array", () => {
      const res = transformAdvancedConfigEntries("{}");
      expect(res).toEqual([]);
    });

    it.each([
      {
        configType: "string",
        configValue: "delete",
        expectedValue: "delete",
      },
      {
        configType: "number",
        configValue: 200,
        expectedValue: "200",
      },
      {
        configType: "boolean",
        configValue: true,
        expectedValue: "true",
      },
    ])(
      "coerces item with type $configType into string",
      ({ configValue, expectedValue }) => {
        const res = transformAdvancedConfigEntries(
          JSON.stringify({
            "test.key": configValue,
          })
        );
        expect(res.find((c) => c.configKey === "test.key")?.configValue).toBe(
          expectedValue
        );
      }
    );

    it.each([
      {
        configKey: "cleanup.policy",
        configType: "null",
        configValue: null,
      },
      {
        configKey: "cleanup.policy",
        configType: "undefined",
        configValue: undefined,
      },
      {
        configKey: "cleanup.policy",
        configType: "object",
        configValue: { foo: "bar" },
      },
      {
        configKey: "cleanup.policy",
        configType: "array",
        configValue: ["1"],
      },
    ])("drops items with type $configType", ({ configValue }) => {
      const res = transformAdvancedConfigEntries(
        JSON.stringify({
          "test.key": configValue,
        })
      );
      expect(res.find((c) => c.configKey === "test.key")).toBe(undefined);
    });
  });

  describe("generateTopicNameDescription", () => {
    it("generates the default description for an env without special requirements", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: undefined,
        topicRegex: undefined,
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        "Allowed characters: letter, digit, period, underscore, and hyphen."
      );
    });

    it("generates the description for an env with one topic prefix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: ["prefix_"],
        topicRegex: undefined,
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Prefix name with: "prefix_". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with two topic prefixes", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: ["prefix_", "prefix2_"],
        topicRegex: undefined,
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Prefix name with: "prefix_" or "prefix2_". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with three topic prefixes", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: ["prefix_", "prefix2_", "prefix3_"],
        topicRegex: undefined,
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Prefix name with: "prefix_", "prefix2_" or "prefix3_". Allowed characters: letter, digit, period, underscore,' +
          " and" +
          " hyphen."
      );
    });

    it("generates the description for an env with one topic suffix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: undefined,
        topicRegex: undefined,
        topicSuffix: ["_suffix"],
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Suffix name with: "_suffix". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with two topic suffix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: undefined,
        topicRegex: undefined,
        topicSuffix: ["_suffix", "_suffix2"],
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Suffix name with: "_suffix" or "_suffix2". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with four topic suffix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: false,
        topicPrefix: undefined,
        topicRegex: undefined,
        topicSuffix: ["_suffix", "_suffix2", "_suffix3", "_suffix4"],
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Suffix name with: "_suffix", "_suffix2", "_suffix3" or "_suffix4". Allowed characters: letter, digit,' +
          " period," +
          " underscore," +
          " and hyphen."
      );
    });

    it("generates the description for an env with one regex", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: true,
        topicPrefix: undefined,
        topicRegex: [".*Dev*."],
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Follow name pattern: ".*Dev*.". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with two regex", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: true,
        topicPrefix: undefined,
        topicRegex: [".*Dev*.", ".*Dev2*."],
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Follow name pattern: ".*Dev*." or ".*Dev2*.". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with three regex", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: true,
        topicPrefix: undefined,
        topicRegex: [".*Dev*.", ".*Dev2*.", ".*Dev3*."],
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Follow name pattern: ".*Dev*.", ".*Dev2*." or ".*Dev3*.". Allowed characters: letter, digit, period,' +
          " underscore, and" +
          " hyphen."
      );
    });

    // if a topic name has a regex format, it can't have a
    // prefix or suffix
    it("generates the description for an env with one regex and a prefix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: true,
        topicPrefix: ["prefix_"],
        topicRegex: [".*Dev*."],
        topicSuffix: undefined,
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Follow name pattern: ".*Dev*.". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });

    it("generates the description for an env with one prefix and two suffix", () => {
      const envWithoutExtras: Environment["params"] = {
        applyRegex: undefined,
        topicPrefix: ["prefix_"],
        topicRegex: undefined,
        topicSuffix: ["_suffix, _suffix"],
      };

      expect(generateTopicNameDescription(envWithoutExtras)).toEqual(
        'Suffix name with: "_suffix, _suffix". Prefix name with: "prefix_". Allowed characters: letter, digit, period, underscore, and hyphen.'
      );
    });
  });

  describe("generateNamePatternString", () => {
    it("creates a string listing one pattern that can be used in a sentence", () => {
      const input = ["prefix1_"];

      expect(generateNamePatternString(input)).toEqual('"prefix1_"');
    });

    it("creates a string listing two pattern that can be used in a sentence", () => {
      const input = ["_suffix1", "_suffix2"];

      expect(generateNamePatternString(input)).toEqual(
        '"_suffix1" or "_suffix2"'
      );
    });

    it("creates a string listing three pattern that can be used in a sentence", () => {
      const input = ["prefix1_", "prefix2_", "prefix3_"];

      expect(generateNamePatternString(input)).toEqual(
        '"prefix1_", "prefix2_" or "prefix3_"'
      );
    });

    it("creates a string listing four pattern that can be used in a sentence", () => {
      const input = ["_suffix1", "_suffix2", "_suffix3", "_suffix4"];

      expect(generateNamePatternString(input)).toEqual(
        '"_suffix1", "_suffix2", "_suffix3" or "_suffix4"'
      );
    });
  });
});
