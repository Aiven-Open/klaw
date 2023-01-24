import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";

describe("environment-transformer.ts", () => {
  describe("transformEnvironmentApiResponse", () => {
    it("transforms API response objects into application domain model", () => {
      const testInput = [
        createMockEnvironmentDTO({
          name: "DEV",
          id: "1001",
          defaultPartitions: "1",
          topicprefix: "dev-",
        }),
      ];

      expect(transformEnvironmentApiResponse(testInput)).toEqual([
        {
          name: "DEV",
          id: "1001",
          defaultPartitions: 1,
          defaultReplicationFactor: undefined,
          maxPartitions: undefined,
          maxReplicationFactor: undefined,
          topicNamePrefix: "dev-",
          topicNameSuffix: undefined,
          type: "kafka",
        },
      ]);
    });
  });
});
