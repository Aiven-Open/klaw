import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";
import { render, cleanup } from "@testing-library/react";
import { ClusterDetails } from "src/app/features/topics/details/overview/components/ClusterDetails";
import {
  getByTermInList,
  getDefinitionList,
  getAllDefinitions,
} from "src/services/test-utils/custom-queries";

const testClusterDetails: ClusterDetailsType = {
  allPageNos: [],
  bootstrapServers: "kafka-112233aa-dev-sandbeach.aivencloud.com:12345",
  clusterId: 999,
  clusterName: "DEV",
  clusterType: "Kafka",
  kafkaFlavor: "Aiven for Kafka",
  protocol: "SSL",
  showDeleteCluster: false,
  totalNoPages: "2",
};

describe("ClusterDetails", () => {
  describe("shows all required elements", () => {
    let definitionList: HTMLDListElement;
    beforeAll(() => {
      const component = render(
        <ClusterDetails clusterDetails={testClusterDetails} />
      );
      definitionList = getDefinitionList(component);
    });

    afterAll(cleanup);

    it("shows a definition list", () => {
      expect(definitionList).toBeVisible();
    });

    it('shows one definition for "Cluster name"', () => {
      const term = getByTermInList(definitionList, "Cluster name");
      const definition = getAllDefinitions(definitionList, "Cluster name");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(testClusterDetails.clusterName);
    });

    it('shows one definition for "Cluster id"', () => {
      const term = getByTermInList(definitionList, "Cluster id");
      const definition = getAllDefinitions(definitionList, "Cluster id");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.clusterId)
      );
    });

    it('shows one definition for "Bootstrap server"', () => {
      const term = getByTermInList(definitionList, "Bootstrap server");
      const definition = getAllDefinitions(definitionList, "Bootstrap server");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.bootstrapServers)
      );
    });

    it('shows one definition for "Type"', () => {
      const term = getByTermInList(definitionList, "Type");
      const definition = getAllDefinitions(definitionList, "Type");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.clusterType)
      );
    });

    it('shows one definition for "Kafka flavor"', () => {
      const term = getByTermInList(definitionList, "Kafka flavor");
      const definition = getAllDefinitions(definitionList, "Kafka flavor");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.kafkaFlavor)
      );
    });
  });

  describe("shows additional content dependent on props", () => {
    let definitionList: HTMLDListElement;
    beforeAll(() => {
      const component = render(
        <ClusterDetails
          clusterDetails={{
            ...testClusterDetails,
            associatedServers: "https://example.com",
          }}
        />
      );
      definitionList = getDefinitionList(component);
    });

    afterAll(cleanup);

    it("shows a definition list", () => {
      expect(definitionList).toBeVisible();
    });

    it('shows one optional definition for "Rest API"', () => {
      const term = getByTermInList(definitionList, "Rest API");
      const definition = getAllDefinitions(definitionList, "Rest API");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent("https://example.com");
    });
  });
});
