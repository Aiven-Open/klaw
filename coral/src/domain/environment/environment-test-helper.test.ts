import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";

describe("environment-test-helper.ts", () => {
  describe("createMockEnvironmentDTO", () => {
    it("creates a mocked EnvironmentDTO object with a given name", () => {
      const nameToTest = "TST";
      const result = {
        allPageNos: ["1"],
        clusterId: 1,
        clusterName: "DEV",
        defaultPartitions: null,
        defaultReplicationFactor: null,
        envStatus: "ONLINE",
        id: "1",
        maxPartitions: null,
        maxReplicationFactor: null,
        name: nameToTest,
        otherParams:
          "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
        showDeleteEnv: false,
        tenantId: 101,
        tenantName: "default",
        topicprefix: null,
        topicsuffix: null,
        totalNoPages: "1",
        type: "kafka",
      };

      expect(createMockEnvironmentDTO({ name: nameToTest })).toEqual(result);
    });
  });
});
