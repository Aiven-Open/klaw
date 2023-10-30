import { cleanup, screen, within } from "@testing-library/react";
import SchemaRegistryEnvironmentsTable from "src/app/features/configuration/environments/SchemaRegistry/components/SchemaRegistryEnvironmentsTable";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { Environment } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const TEST_UPDATE_TIME = "14-Sep-2023 12:30:38 UTC";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const mockEnvironments: Environment[] = [
  createMockEnvironmentDTO({
    type: "schemaregistry",
    name: "DEV_SCH",
    id: "1",
    clusterName: "DEV_CL",
    envStatus: "ONLINE",
    associatedEnv: { id: "1", name: "DEV" },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    type: "schemaregistry",
    name: "TST_SCH",
    id: "2",
    clusterName: "TST_CL",
    envStatus: "OFFLINE",
    associatedEnv: { id: "2", name: "TST" },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    type: "schemaregistry",
    name: "PROD_SCH",
    id: "3",
    clusterName: "PROD_CL",
    envStatus: "NOT_KNOWN",
    associatedEnv: { id: "3", name: "PROD" },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
];

const tableRowHeader = [
  "Environment",
  "Cluster",
  "Tenant",
  "Associated Kafka Environment",
  "Status",
];

describe("SchemaRegistryEnvironmentsTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);
    it("show empty state when there is no data", () => {
      customRender(
        <SchemaRegistryEnvironmentsTable
          environments={[]}
          ariaLabel={"Schema Registry Environments overview, page 0 of 0"}
        />,
        { queryClient: true }
      );
      expect(
        screen.getByRole("heading", {
          name: "No Schema Registry Environments",
        })
      ).toBeVisible();
    });
  });

  describe("shows all environments as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();

      customRender(
        <SchemaRegistryEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Schema Registry Environments overview, page 1 of 10"}
        />,
        { queryClient: true }
      );
    });

    afterAll(cleanup);

    it("renders a environment table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Schema Registry Environments overview, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema Registry Environments overview, page 1 of 10",
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
          name: "Schema Registry Environments overview, page 1 of 10",
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
          name: "Schema Registry Environments overview, page 1 of 10",
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
          name: "Schema Registry Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const tenant = within(row).getByRole("cell", {
          name: environment.tenantName,
        });

        expect(tenant).toBeVisible();
      });

      it(`renders the associated Environment for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Schema Registry Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const tenant = within(row).getByRole("cell", {
          name: environment.associatedEnv?.name,
        });

        expect(tenant).toBeVisible();
      });

      it(`renders the status for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Schema Registry Environments overview, page 1 of 10",
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
          name: `${statusText} Last update: ${TEST_UPDATE_TIME} UTC`,
        });

        expect(status).toBeVisible();
      });
    });
  });
});
