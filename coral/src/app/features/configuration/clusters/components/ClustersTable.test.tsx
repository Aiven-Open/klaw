import { cleanup, render, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { ClusterDetails } from "src/domain/cluster";
import { ClustersTable } from "src/app/features/configuration/clusters/components/ClustersTable";

const testCluster: ClusterDetails[] = [
  {
    allPageNos: ["1"],
    associatedServers: "https://testrestproxy:11111",
    bootstrapServers: "this-is-a-test.aivencloud.com:11111",
    clusterId: 1,
    clusterName: "DEV",
    clusterStatus: "OFFLINE",
    clusterType: "kafka",
    kafkaFlavor: "Aiven for Apache Kafka",
    projectName: "test-project-name",
    protocol: "SSL",
    publicKey: "",
    serviceName: "test-service-name",
    showDeleteCluster: true,
    totalNoPages: "1",
    currentPage: "1",
  },
  {
    allPageNos: ["1"],
    associatedServers: "https://otherproxy:11111",
    bootstrapServers: "11.111.11.111:9999",
    clusterId: 2,
    clusterName: "PROD",
    clusterStatus: "OFFLINE",
    clusterType: "kafkaconnect",
    kafkaFlavor: "Apache Kafka",
    protocol: "PLAINTEXT",
    publicKey: "",
    showDeleteCluster: true,
    totalNoPages: "1",
    currentPage: "1",
  },
];

const tableRowHeader = [
  "Cluster",
  "Bootstrap servers",
  "Protocol",
  "Type",
  "RestApi server",
  "Other params",
];

describe("ClusterTable.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("shows empty state correctly", () => {
    beforeAll(() => {
      render(<ClustersTable clusters={[]} ariaLabel={""} />);
    });

    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      const headline = screen.getByRole("heading", {
        name: "No Clusters",
      });

      expect(headline).toBeVisible();
    });

    it("show information to user when there is no data", () => {
      const infoText = screen.getByText("No clusters found.");
      expect(infoText).toBeVisible();
    });
  });

  describe("shows all clusters as a table", () => {
    const tableLabel = "Cluster overview";
    beforeAll(() => {
      customRender(
        <ClustersTable clusters={testCluster} ariaLabel={tableLabel} />,
        { queryClient: true }
      );
    });

    afterAll(cleanup);

    it("renders a table with an acessible name", async () => {
      const table = screen.getByRole("table", {
        name: tableLabel,
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    testCluster.forEach((cluster) => {
      it(`renders the cluster name "${cluster.clusterName}" as row header`, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const rowHeader = within(table).getByRole("cell", {
          name: cluster.clusterName,
        });
        const name = within(rowHeader).getByText(cluster.clusterName);

        expect(rowHeader).toBeVisible();
        expect(name).toBeVisible();
      });

      it(`renders the bootstrap servers for ${cluster.clusterName} `, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${cluster.clusterName}`, "i"),
        });
        const bootstrapServer = within(row).getByRole("cell", {
          name: cluster.bootstrapServers,
        });

        expect(bootstrapServer).toBeVisible();
      });

      it(`renders the protocol for ${cluster.clusterName}`, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${cluster.clusterName}`, "i"),
        });
        const protocol = within(row).getByRole("cell", {
          name: cluster.protocol,
        });

        expect(protocol).toBeVisible();
      });

      it(`renders the type for ${cluster.clusterName}`, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${cluster.clusterName}`, "i"),
        });
        const type = within(row).getByRole("cell", {
          name: cluster.clusterType,
        });

        expect(type).toBeVisible();
      });

      it(`renders the kafka flavor for ${cluster.clusterName}`, () => {
        const table = screen.getByRole("table", {
          name: tableLabel,
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${cluster.clusterName}`, "i"),
        });
        const kafkaFlavor = within(row).getByRole("cell", {
          name: cluster.kafkaFlavor,
        });

        expect(kafkaFlavor).toBeVisible();
      });

      if (cluster.associatedServers)
        it(`renders the rest api servers for ${cluster.clusterName}`, () => {
          const table = screen.getByRole("table", {
            name: tableLabel,
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${cluster.clusterName}`, "i"),
          });
          const restApiServer = within(row).getByRole("cell", {
            name: cluster.associatedServers,
          });

          expect(restApiServer).toBeVisible();
        });

      if (cluster.serviceName) {
        it(`optionally renders the serviceName as other params for ${cluster.clusterName}`, () => {
          const table = screen.getByRole("table", {
            name: tableLabel,
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${cluster.clusterName}`, "i"),
          });
          const serviceNameRegex = new RegExp(
            `serviceName=${cluster.serviceName}`
          );

          const serviceName = within(row).getByRole("cell", {
            name: serviceNameRegex,
          });

          expect(serviceName).toBeVisible();
        });
      }

      if (cluster.projectName) {
        it(`optionally renders the projectName as other params for ${cluster.clusterName}`, () => {
          const table = screen.getByRole("table", {
            name: tableLabel,
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${cluster.clusterName}`, "i"),
          });
          const projectNameRegex = new RegExp(
            `projectName=${cluster.projectName}`
          );

          const projectName = within(row).getByRole("cell", {
            name: projectNameRegex,
          });

          expect(projectName).toBeVisible();
        });
      }
    });
  });
});
