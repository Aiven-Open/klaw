import {
  createTopicRequestPayload,
  transformAdvancedConfigEntries,
} from "src/app/features/topics/request/utils";

describe("TopicRequest utils", () => {
  describe("createTopicRequestPayload", () => {
    it("returns expected payload", () => {
      const formData = {
        environment: {
          id: "1",
          name: "DEV",
          defaultPartitions: 2,
          maxPartitions: 6,
          defaultReplicationFactor: 3,
          maxReplicationFactor: 3,
        },
        remarks: "please approve this topic asap",
        replicationfactor: "2",
        topicpartitions: "3",
        topicname: "example-topic-name",
        description: "example description",
        advancedConfiguration: "{}",
      };
      const res = createTopicRequestPayload(formData);

      expect(res).toEqual({
        environment: "1",
        topicname: "example-topic-name",
        replicationfactor: "2",
        topicpartitions: 3,
        advancedTopicConfigEntries: [],
        description: "example description",
        remarks: "please approve this topic asap",
        requestOperationType: "CREATE",
      });
    });
  });
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
});
