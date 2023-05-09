import { cleanup, screen, render, within } from "@testing-library/react";
import ConnectorTable from "src/app/features/connectors/browse/components/ConnectorTable";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { tabThroughForward } from "src/services/test-utils/tabbing";
import { Connector } from "src/domain/connector";

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
    environmentsList: ["DEV"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
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
    environmentsList: ["DEV", "TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
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
    environmentsList: ["TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
];

const tableRowHeader = ["Connector", "Environments", "Team"];

describe("ConnectorTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      render(
        <ConnectorTable
          connectors={[]}
          ariaLabel={"Kafka Connector overview, page 0 of 0"}
        />
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
      render(
        <ConnectorTable
          connectors={mockConnectors}
          ariaLabel={"Connectors overview, page 1 of 10"}
        />
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
          `/connectorOverview?connectorName=${connector.connectorName}`
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
          name: (connector.environmentsList as string[]).join(" "),
        });

        expect(environmentList).toBeVisible();
      });
      // environmentList could be undefined, but isn't in our usage here
      // so this is needed to prevent type errors
      (connector.environmentsList as string[]).forEach((env) => {
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
            name: (connector.environmentsList as string[]).join(" "),
          });

          expect(environmentList).toBeVisible();
        });
      });
    });
  });

  describe("enables user to keyboard navigate from connector name to connector name", () => {
    beforeEach(() => {
      mockIntersectionObserver();
      render(
        <ConnectorTable
          connectors={mockConnectors}
          ariaLabel={"Connectors overview, page 1 of 10"}
        />
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
