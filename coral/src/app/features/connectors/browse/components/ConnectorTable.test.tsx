import { cleanup, screen, within } from "@testing-library/react";
import ConnectorTable from "src/app/features/connectors/browse/components/ConnectorTable";
import { Connector } from "src/domain/connector";
import { EnvironmentInfo } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabThroughForward } from "src/services/test-utils/tabbing";

const mockConnectors: Connector[] = [
  {
    sequence: 2,
    connectorId: 1001,
    connectorName: "test_connector_1",
    environmentId: "4",
    teamName: "Dev",
    teamId: 1,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: [{ id: "1", name: "DEV" }],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
    connectorStatus: "",
    runningTasks: 1,
    failedTasks: 0,
  },
  {
    sequence: 2,
    connectorId: 1002,
    connectorName: "test_connector_2",
    environmentId: "4",
    teamName: "Ospo",
    teamId: 2,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: [
      { id: "1", name: "DEV" },
      { id: "2", name: "TST" },
    ],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
    connectorStatus: "",
    runningTasks: 1,
    failedTasks: 0,
  },
  {
    sequence: 2,
    connectorId: 1003,
    connectorName: "test_connector_3",
    environmentId: "4",
    teamName: "Infra",
    teamId: 3,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: [{ id: "2", name: "TST" }],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
    connectorStatus: "",
    runningTasks: 1,
    failedTasks: 0,
  },
];

const tableRowHeader = ["Connector", "Environments", "Team"];

const isFeatureFlagActiveMock = jest.fn();
jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

describe("ConnectorTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      customRender(
        <ConnectorTable
          connectors={[]}
          ariaLabel={"Kafka Connector overview, page 0 of 0"}
        />,
        { memoryRouter: true }
      );
      expect(
        screen.getByRole("heading", {
          name: "No Connectors",
        })
      ).toBeVisible();
    });
  });

  describe("shows all Connectors as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      isFeatureFlagActiveMock.mockReturnValue(true);

      customRender(
        <ConnectorTable
          connectors={mockConnectors}
          ariaLabel={"Connectors overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    it("renders a connector table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Connectors overview, page 1 of 10",
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockConnectors.forEach((connector) => {
      it(`renders the connector name "${connector.connectorName}" as a link to the detail view as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Connectors overview, page 1 of 10",
        });
        const rowHeader = within(table).getByRole("cell", {
          name: connector.connectorName,
        });
        const link = within(rowHeader).getByRole("link", {
          name: connector.connectorName,
        });

        expect(rowHeader).toBeVisible();
        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/connector/${connector.connectorName}/overview`
        );
      });

      it(`renders the team for ${connector.connectorName} `, () => {
        const table = screen.getByRole("table", {
          name: "Connectors overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${connector.connectorName}`, "i"),
        });
        const team = within(row).getByRole("cell", {
          name: connector.teamName,
        });

        expect(team).toBeVisible();
      });

      it(`renders a list of Environments for connector ${connector}`, () => {
        const table = screen.getByRole("table", {
          name: "Connectors overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${connector.connectorName}`, "i"),
        });
        const environmentList = within(row).getByRole("cell", {
          // environmentList could be undefined, but isn't in our usage here
          // so this is needed to prevent type errors
          name: (connector.environmentsList as EnvironmentInfo[]).join(" "),
        });

        expect(environmentList).toBeVisible();
      });
      // environmentList could be undefined, but isn't in our usage here
      // so this is needed to prevent type errors
      (connector.environmentsList as EnvironmentInfo[]).forEach((env) => {
        it(`renders Environment ${env} for connector ${connector}`, () => {
          const table = screen.getByRole("table", {
            name: "Connectors overview, page 1 of 10",
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${connector.connectorName}`, "i"),
          });
          const environmentList = within(row).getByRole("cell", {
            // environmentList could be undefined, but isn't in our usage here
            // so this is needed to prevent type errors
            name: (connector.environmentsList as EnvironmentInfo[]).join(" "),
          });

          expect(environmentList).toBeVisible();
        });
      });
    });
  });

  describe("renders link to angular app if feature-flag is not enabled", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      isFeatureFlagActiveMock.mockReturnValue(false);

      customRender(
        <ConnectorTable
          connectors={mockConnectors}
          ariaLabel={"Connectors overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    mockConnectors.forEach((connector) => {
      it(`renders the connector name "${connector.connectorName}" as a link to the detail view as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Connectors overview, page 1 of 10",
        });
        const rowHeader = within(table).getByRole("cell", {
          name: connector.connectorName,
        });
        const link = within(rowHeader).getByRole("link", {
          name: connector.connectorName,
        });

        expect(rowHeader).toBeVisible();
        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `http://localhost/connectorOverview?connectorName=${connector.connectorName}`
        );
      });
    });
  });

  describe("enables user to keyboard navigate from connector name to connector name", () => {
    beforeEach(() => {
      mockIntersectionObserver();
      customRender(
        <ConnectorTable
          connectors={mockConnectors}
          ariaLabel={"Connectors overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 10",
      });
      table.focus();
    });

    afterEach(cleanup);

    mockConnectors.forEach((connector, index) => {
      const numbersOfTabs = index + 1;
      it(`sets focus on "${connector.connectorName}" when user tabs ${numbersOfTabs} times`, async () => {
        const link = screen.getByRole("link", {
          name: connector.connectorName,
        });

        expect(link).not.toHaveFocus();

        await tabThroughForward(numbersOfTabs);

        expect(link).toHaveFocus();
      });
    });
  });
});
