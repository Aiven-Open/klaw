import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";
import { render, cleanup, RenderResult, screen } from "@testing-library/react";
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
  clusterStatus: "NOT_KNOWN",
};

describe("ClusterDetails", () => {
  describe("shows all required elements", () => {
    let definitionList: HTMLDListElement;
    beforeAll(() => {
      const component = render(
        <ClusterDetails
          clusterDetails={testClusterDetails}
          isUpdating={false}
        />
      );
      definitionList = getDefinitionList(component);
    });

    afterAll(cleanup);

    it("shows a definition list", () => {
      expect(definitionList).toBeVisible();
    });

    it('shows one definition for "Bootstrap server"', () => {
      const term = getByTermInList(definitionList, "Bootstrap server");
      const definition = getAllDefinitions(definitionList, "Bootstrap server");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.bootstrapServers)
      );
    });

    it('shows one definition for "Protocol"', () => {
      const term = getByTermInList(definitionList, "Protocol");
      const definition = getAllDefinitions(definitionList, "Protocol");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(
        String(testClusterDetails.protocol)
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

    it('shows one placeholder definition for optional "Rest API" when value not set', () => {
      const term = getByTermInList(definitionList, "Rest API");
      const definition = getAllDefinitions(definitionList, "Rest API");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent("Not applicable");
    });

    it('shows one definition for "Cluster name"', () => {
      const term = getByTermInList(definitionList, "Cluster name");
      const definition = getAllDefinitions(definitionList, "Cluster name");

      expect(term).toBeVisible();
      expect(definition[0]).toHaveTextContent(testClusterDetails.clusterName);
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
          isUpdating={false}
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

  describe("shows loading state when details are updating", () => {
    describe('when "isUpdating" is false', () => {
      let component: RenderResult;
      beforeAll(() => {
        component = render(
          <ClusterDetails
            clusterDetails={testClusterDetails}
            isUpdating={false}
          />
        );
      });

      afterAll(cleanup);
      it("does not render loading information", () => {
        const info = screen.queryByText("Cluster details are updating");

        expect(info).not.toBeInTheDocument();
      });

      it("does not hide definition list from assistive technology", () => {
        const definitionList = getDefinitionList(component);
        expect(definitionList.parentElement).not.toHaveAttribute(
          "aria-hidden",
          "true"
        );
      });
    });

    describe('when "isUpdating" is true', () => {
      let component: RenderResult;
      beforeAll(() => {
        component = render(
          <ClusterDetails
            clusterDetails={testClusterDetails}
            isUpdating={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows loading information for assistive technology", () => {
        const info = screen.getByText("Cluster details are updating");

        expect(info).toBeVisible();
        expect(info).toHaveClass("visually-hidden");
      });

      it("hides the definition list from assitive technology", () => {
        const list = getDefinitionList(component);
        expect(list.parentElement).toHaveAttribute("aria-hidden", "true");
      });
    });
  });

  describe("logs error for developers if they pass wrong props", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
    });

    it("logs no error if isUpdating is true and cluster details undefined", () => {
      render(<ClusterDetails clusterDetails={undefined} isUpdating={true} />);

      expect(console.error).not.toHaveBeenCalled();
    });

    it("logs no error if isUpdating is true and cluster details defined", () => {
      render(
        <ClusterDetails clusterDetails={testClusterDetails} isUpdating={true} />
      );

      expect(console.error).not.toHaveBeenCalled();
    });

    it("logs no error if isUpdating is false and cluster details defined", () => {
      render(
        <ClusterDetails
          clusterDetails={testClusterDetails}
          isUpdating={false}
        />
      );

      expect(console.error).not.toHaveBeenCalled();
    });

    it("logs error if isUpdating is true and cluster details undefined", () => {
      render(<ClusterDetails clusterDetails={undefined} isUpdating={false} />);

      expect(console.error).toHaveBeenCalledWith(
        "You must pass cluster details when the state of isUpdating is true"
      );
    });
  });
});
