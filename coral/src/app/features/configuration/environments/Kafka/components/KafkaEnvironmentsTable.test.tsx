import { cleanup, screen, within } from "@testing-library/react";
import KafkaEnvironmentsTable from "src/app/features/configuration/environments/Kafka/components/KafkaEnvironmentsTable";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { Environment } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { UseAuthContext } from "src/app/context-provider/AuthProvider";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";

const TEST_UPDATE_TIME = "${TEST_UPDATE_TIME}";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const mockEnvironments: Environment[] = [
  createMockEnvironmentDTO({
    name: "DEV",
    id: "1",
    clusterName: "DEV_CL",
    envStatus: "ONLINE",
    params: {
      defaultPartitions: "1",
      defaultRepFactor: "2",
      maxPartitions: "3",
      maxRepFactor: "4",
    },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    name: "TST",
    id: "2",
    clusterName: "TST_CL",
    envStatus: "OFFLINE",
    params: {
      defaultPartitions: "2",
      defaultRepFactor: "4",
      maxPartitions: "3",
      maxRepFactor: "5",
    },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
  createMockEnvironmentDTO({
    name: "PROD",
    id: "3",
    clusterName: "PROD_CL",
    envStatus: "NOT_KNOWN",
    params: {
      defaultPartitions: "5",
      defaultRepFactor: "7",
      maxPartitions: "6",
      maxRepFactor: "8",
    },
    envStatusTimeString: TEST_UPDATE_TIME,
  }),
];

const tableRowHeader = [
  "Environment",
  "Cluster",
  "Tenant",
  "Replication factor",
  "Partition",
  "Status",
];

let mockAuthUserContext: UseAuthContext = {
  ...testAuthUser,
  isSuperAdminUser: false,
};
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUserContext,
}));

describe("KafkaEnvironmentsTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);
    it("show empty state when there is no data", () => {
      customRender(
        <KafkaEnvironmentsTable
          environments={[]}
          ariaLabel={"Kafka Environments overview, page 0 of 0"}
        />,
        { queryClient: true }
      );
      expect(
        screen.getByRole("heading", {
          name: "No Kafka Environments",
        })
      ).toBeVisible();
    });
  });

  describe("shows all environments as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();

      customRender(
        <KafkaEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Environments overview, page 1 of 10"}
        />,
        { queryClient: true }
      );
    });

    afterAll(cleanup);

    it("renders a environment table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Environments overview, page 1 of 10",
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
          name: "Kafka Environments overview, page 1 of 10",
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
          name: "Kafka Environments overview, page 1 of 10",
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
          name: "Kafka Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const tenant = within(row).getByRole("cell", {
          name: environment.tenantName,
        });

        expect(tenant).toBeVisible();
      });

      it(`renders a list of default and max replication factor for environment ${environment}`, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const replicationFactors = within(row).getByRole("cell", {
          name: `Default: ${environment.params?.defaultRepFactor} Max: ${environment.params?.maxRepFactor}`,
        });

        expect(replicationFactors).toBeVisible();
      });

      it(`renders a list of default and max partitions for environment ${environment}`, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Environments overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${environment.name}`, "i"),
        });
        const partitions = within(row).getByRole("cell", {
          name: `Default: ${environment.params?.defaultPartitions} Max: ${environment.params?.maxPartitions}`,
        });

        expect(partitions).toBeVisible();
      });
      it(`renders the status for ${environment.name} `, () => {
        const table = screen.getByRole("table", {
          name: "Kafka Environments overview, page 1 of 10",
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
        <KafkaEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Environments overview, page 1 of 10"}
        />,
        { queryClient: true }
      );

      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 1 of 10",
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
        <KafkaEnvironmentsTable
          environments={mockEnvironments}
          ariaLabel={"Kafka Environments overview, page 1 of 10"}
        />,
        { queryClient: true }
      );

      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 1 of 10",
      });

      const columns = within(table).getAllByRole("columnheader");

      expect(columns).toHaveLength(tableRowHeader.length);
    });
  });
});
