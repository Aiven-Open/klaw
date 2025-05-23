import { cleanup, screen, within } from "@testing-library/react";
import KafkaConnectEnvironmentsTable from "src/app/features/configuration/environments/KafkaConnect/components/KafkaConnectEnvironmentsTable";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { Environment } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { UseAuthContext } from "src/app/context-provider/AuthProvider";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";

const TEST_UPDATE_TIME = "14-Sep-2023 12:30:38 UTC";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const mockEnvironments: Environment[] = [
  createMockEnvironmentDTO({
    type: "kafkaconnect",
    name: "DEV",
    id: "1",
    clusterName: "DEV_CL",
    envStatus: "ONLINE",
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    type: "kafkaconnect",
    name: "TST",
    id: "2",
    clusterName: "TST_CL",
    envStatus: "OFFLINE",
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    type: "kafkaconnect",
    name: "PROD",
    id: "3",
    clusterName: "PROD_CL",
    envStatus: "NOT_KNOWN",
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
];

const tableRowHeader = ["Environment", "Cluster", "Tenant", "Status"];

let mockAuthUserContext: UseAuthContext = {
  ...testAuthUser,
  isSuperAdminUser: false,
};
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUserContext,
}));

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
            ? "Online"
            : environment.envStatus === "OFFLINE"
              ? "Offline"
              : "Unknown";

        const status = within(row).getByRole("cell", {
          name: `${statusText} Last update: ${TEST_UPDATE_TIME} UTC`,
        });

        expect(status).toBeVisible();
      });
    });
  });

  describe("shows additional colum with edit link for superadmin user", () => {
    beforeAll(() => {
      mockIntersectionObserver();
    });

    afterEach(cleanup);

    const additionalRowSuperAdmin = "Manage";
    const tableRowHeaderSuperAdmin = [
      ...tableRowHeader,
      additionalRowSuperAdmin,
    ];

    it("shows a row with edit link for superadmin user", () => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: true };
      customRender(
        <KafkaConnectEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Connect Environments overview, page 0 of 0"}
        />,
        { queryClient: true }
      );

      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 0 of 0",
      });

      const columns = within(table).getAllByRole("columnheader");

      expect(columns).toHaveLength(tableRowHeaderSuperAdmin.length);
      expect(columns[tableRowHeaderSuperAdmin.length - 1]).toHaveTextContent(
        additionalRowSuperAdmin
      );
    });

    it("does not show the colum for user", () => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: false };
      customRender(
        <KafkaConnectEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Connect Environments overview, page 0 of 0"}
        />,
        { queryClient: true }
      );

      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 0 of 0",
      });

      const columns = within(table).getAllByRole("columnheader");
      expect(columns).toHaveLength(tableRowHeader.length);
    });
  });
});
