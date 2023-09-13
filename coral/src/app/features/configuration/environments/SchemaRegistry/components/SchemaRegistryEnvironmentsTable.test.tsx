import { cleanup, render, screen, within } from "@testing-library/react";
import SchemaRegistryEnvironmentsTable from "src/app/features/configuration/environments/SchemaRegistry/components/SchemaRegistryEnvironmentsTable";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { Environment } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockEnvironments: Environment[] = [
  createEnvironment({
    type: "schemaregistry",
    name: "DEV_SCH",
    id: "1",
    clusterName: "DEV_CL",
    envStatus: "ONLINE",
    associatedEnv: { id: "1", name: "DEV" },
  }),
  createEnvironment({
    type: "schemaregistry",
    name: "TST_SCH",
    id: "2",
    clusterName: "TST_CL",
    envStatus: "OFFLINE",
    associatedEnv: { id: "2", name: "TST" },
  }),
  createEnvironment({
    type: "schemaregistry",
    name: "PROD_SCH",
    id: "3",
    clusterName: "PROD_CL",
    envStatus: "NOT_KNOWN",
    associatedEnv: { id: "3", name: "PROD" },
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
      render(
        <SchemaRegistryEnvironmentsTable
          environments={[]}
          ariaLabel={"Schema Registry Environments overview, page 0 of 0"}
        />
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

      render(
        <SchemaRegistryEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Schema Registry Environments overview, page 1 of 10"}
        />
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
          name: statusText,
        });

        expect(status).toBeVisible();
      });
    });
  });
});
