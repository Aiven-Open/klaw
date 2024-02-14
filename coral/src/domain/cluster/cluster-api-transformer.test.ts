import { transformPaginatedClustersApiResponse } from "src/domain/cluster/cluster-api-transformer";
import { KlawApiModel } from "types/utils";

const mockedApiResponse: KlawApiModel<"KwClustersModelResponse">[] = [
  {
    clusterId: 1,
    clusterName: "DEV",
    bootstrapServers: "test.aivencloud.com:11111",
    protocol: "SSL",
    clusterType: "KAFKA",
    kafkaFlavor: "AIVEN_FOR_APACHE_KAFKA",
    showDeleteCluster: true,
    totalNoPages: "1",
    allPageNos: ["1"],
    clusterStatus: "OFFLINE",
    associatedServers: "https://testrestproxy:11111",
    projectName: "nice-project",
    serviceName: "kafka-service",
    currentPage: "1",
  },
  {
    clusterId: 7,
    clusterName: "TST_SCHEMA",
    bootstrapServers: "some-sandbox.internal:9999",
    protocol: "PLAINTEXT",
    clusterType: "SCHEMA_REGISTRY",
    kafkaFlavor: "AIVEN_FOR_APACHE_KAFKA",
    showDeleteCluster: true,
    totalNoPages: "1",
    allPageNos: ["1"],
    clusterStatus: "OFFLINE",
    currentPage: "1",
  },
];

describe("cluster-api-transformer.ts", () => {
  describe("transformPaginatedClustersApiResponse", () => {
    it("transforms a KwClustersModelResponse into a pagianted response", () => {
      const result = transformPaginatedClustersApiResponse(mockedApiResponse);

      expect(result).toEqual({
        totalPages: Number(mockedApiResponse[0].totalNoPages),
        //@TODO update test when backend change is in
        currentPage: 1,
        entries: mockedApiResponse,
      });
    });
  });
});
