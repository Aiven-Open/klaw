import { cleanup, screen, within } from "@testing-library/react";
import KafkaConnectEnvironmentsTable from "src/app/features/configuration/environments/KafkaConnect/components/KafkaConnectEnvironmentsTable";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { Environment } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const mockEnvironments: Environment[] = [
  createEnvironment({
    type: "kafkaconnect",
    name: "DEV",
    id: "1",
    clusterName: "DEV_CL",
    envStatus: "ONLINE",
  }),
  createEnvironment({
    type: "kafkaconnect",
    name: "TST",
    id: "2",
    clusterName: "TST_CL",
    envStatus: "OFFLINE",
  }),
  createEnvironment({
    type: "kafkaconnect",
    name: "PROD",
    id: "3",
    clusterName: "PROD_CL",
    envStatus: "NOT_KNOWN",
  }),
];

const tableRowHeader = ["Environment", "Cluster", "Tenant", "Status"];

describe("KafkaConnectEnvironmentsTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);
    it("show empty state when there is no data", () => {
      customRender(
        <KafkaConnectEnvironmentsTable
          environments={[]}
          ariaLabel={"Kafka Connect Environments overview, page 0 of 0"}
        />,
        { queryClient: true }
      );
      expect(
        screen.getByRole("heading", {
          name: "No Kafka Connect Environments",
        })
      ).toBeVisible();
    });
  });

  describe("shows all environments as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();

      customRender(
        <KafkaConnectEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Connect Environments overview, page 1 of 10"}
        />,
        { queryClient: true }
      );
    });

    afterAll(cleanup);

    it("renders a environment table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Connect Environments overview, page 1 of 10",
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockEnvironments.forEach((environment) => {
      it(`renders the environment name "${environment.name}" as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Connect Environments overview, page 1 of 10",
        });
        const rowHeader = within(table).getByRole("cell", {
          name: environment.name,
        });
        const name = within(rowHeader).getByText(environment.name);

        expect(rowHeader).toBeVisible();
        expect(name).toBeVisible();
      });

      it(`renders the cluster for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Connect Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const cluster = within(row).getByRole("cell", {
          name: environment.clusterName,
        });

        expect(cluster).toBeVisible();
      });

      it(`renders the tenant for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Connect Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const tenant = within(row).getByRole("cell", {
          name: environment.tenantName,
        });

        expect(tenant).toBeVisible();
      });

      it(`renders the status for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Connect Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const statusText =
          environment.envStatus === "ONLINE"
            ? "Working"
            : environment.envStatus === "OFFLINE"
            ? "Not working"
            : "Unknown";

        const status = within(row).getByRole("cell", {
          name: statusText,
        });

        expect(status).toBeVisible();
      });
    });
  });
});
