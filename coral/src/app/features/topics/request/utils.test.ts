import {
  generateNamePatternString,
  generateTopicNameDescription,
  transformAdvancedConfigEntries,
  isValidTopicNameWithPrefixAndSuffix,
  isValidTopicNameWithPrefix,
  isValidTopicNameWithSuffix,
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

  describe("isValidTopicNameWithPrefix", () => {
    const defaultPattern = /^[a-zA-Z0-9._-]*$/;

    describe("with one prefix in the list", () => {
      const testPrefix = ["one_"];
      it("returns false when topic name does not contain prefix", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "some-topic",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains prefix, but has no other characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains prefix, but only has 1 character", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_1",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains prefix, but only has 2 characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_12",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when the topic name contains prefix but the rest doesn't match default pattern", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_@#$%",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true when topic name contains prefix, and only has 3 characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_123",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });

    describe("with three prefix in the list", () => {
      const testPrefix = ["one_", "anotherone_", "prefix_"];

      it("returns false when topic name contains one prefix, but has no other characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "anotherone_",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains prefix, but only has 1 character", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_1",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains prefix, but only has 2 characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "anotherone_12",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when the topic name matches but the rest doesn't match default pattern", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "one_@#$%",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true when topic name contains prefix, and only has 3 characters", () => {
        const result = isValidTopicNameWithPrefix({
          topicName: "anotherone_123",
          prefix: testPrefix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });
  });

  describe("isValidTopicNameWithSuffix", () => {
    const defaultPattern = /^[a-zA-Z0-9._-]*$/;

    describe("with one suffix in the list", () => {
      const testSuffix = ["_one"];

      it("returns false when topic name does not contain suffix", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "some-topic",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains suffix, but has no other characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "_one",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains suffix, but only has 1 character", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "1_one",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains suffix, but only has 2 characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "12_one",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when the topic name contains suffix but the rest doesn't match default pattern", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "@#$%_one",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true when topic name contains suffix, and only has 3 characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "abc_one",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });

    describe("with three suffix in the list", () => {
      const testSuffix = ["_suf", "_suffix", "_supersuffix"];

      it("returns false when topic name contains one suffix, but has no other characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "_suf",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains suffix, but only has 1 character", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "1_suffix",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when topic name contains suffix, but only has 2 characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "12_suffix",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false when the topic name matches but the rest doesn't match default pattern", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "@#$%1_su",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true when topic name contains suffix, and has 3 characters", () => {
        const result = isValidTopicNameWithSuffix({
          topicName: "123_suffix",
          suffix: testSuffix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });
  });

  describe("isValidTopicNameWithPrefixAndSuffix", () => {
    const defaultPattern = /^[a-zA-Z0-9._-]*$/;
    it("returns true if there is no prefix or suffix", () => {
      const result = isValidTopicNameWithPrefixAndSuffix({
        topicName: "some-topic",
        prefix: [],
        suffix: [],
        defaultPattern,
      });
      expect(result).toBe(true);
    });

    describe("one prefix and one suffix", () => {
      const prefix = ["one_"];
      const suffix = ["_9one"];

      it("returns false if topicName contains none of those", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "some-topic",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName only contains the prefix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName only contains the suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "_9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains both, but has no character other than prefix and suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one__9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains both, but has only one character other than prefix and suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_1_9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains both, but has only two characters other than prefix and suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_12_9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains one of two similar prefixes, but has only two characters other then that", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "pre_21_su2",
          prefix: ["pre_", "otherpre_"],
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains both, the name minus prefix and suffix have 3 characters, but they don't match the default pattern", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_a-su_",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true if topicName contains both and the part without prefix and suffix has three characters", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_abc_9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });

    describe("multiple prefixes and suffixes", () => {
      const defaultPattern = /^[a-zA-Z0-9._-]*$/;
      const prefix = ["one_", "two_"];
      const suffix = ["_9one", "_9two", "_9three"];

      it("returns false if topicName contains none of those", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "some-topic",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName only contains one of the prefixes", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName only contains one of the suffixes", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "_9one",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains one of both, but has only one character other than prefix and suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one__9two",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains one of both, but has only two characters other than prefix and suffix", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_12_9two",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains one of two similar suffixes, but has only two characters other then that", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_12_su2",
          prefix,
          suffix: ["_su", "_su2"],
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns false if topicName contains one of both, the name minus prefix and suffix have 3 characters, but they don't match the default pattern", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_$!!_9two",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(false);
      });

      it("returns true if topicName contains one of both and the part without prefix and suffix has three characters", () => {
        const result = isValidTopicNameWithPrefixAndSuffix({
          topicName: "one_abc_9two",
          prefix,
          suffix,
          defaultPattern,
        });
        expect(result).toBe(true);
      });
    });
  });
});
