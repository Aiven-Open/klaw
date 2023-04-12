import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";

describe("environment-test-helper.ts", () => {
  describe("createMockEnvironmentDTO", () => {
    it("creates a mocked EnvironmentDTO object with a given name", () => {
      const nameToTest = "TST";
      const result = {
        allPageNos: ["1"],
        clusterId: 1,
        clusterName: "DEV",
        defaultPartitions: undefined,
        defaultReplicationFactor: undefined,
        envStatus: "ONLINE",
        id: "1",
        maxPartitions: undefined,
        maxReplicationFactor: undefined,
        name: nameToTest,
        otherParams:
          "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
        showDeleteEnv: false,
        tenantId: 101,
        tenantName: "default",
        topicprefix: undefined,
        topicsuffix: undefined,
        totalNoPages: "1",
        type: "kafka",
        associatedEnv: undefined,
        clusterType: "ALL",
      };

      expect(createMockEnvironmentDTO({ name: nameToTest })).toEqual(result);
    });
  });
});
