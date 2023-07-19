import { cleanup, screen, within } from "@testing-library/react";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorHistory } from "src/app/features/connectors/details/history/ConnectorHistory";
import { ConnectorOverview } from "src/domain/connector";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const testConnectorHistoryList = [
  {
    environmentName: "DEV",
    teamName: "Richmond1",
    requestedBy: "tedlasso",
    requestedTime: "2022-Nov-04 14:41:18",
    approvedBy: "roykent",
    approvedTime: "2022-Nov-04 14:48:38",
    remarks: "Create",
  },
  {
    environmentName: "DEV",
    teamName: "Richmond2",
    requestedBy: "rebeccawelton",
    requestedTime: "2022-Nov-04 15:41:18",
    approvedBy: "Keeleyjones",
    approvedTime: "2022-Nov-04 15:48:38",
    remarks: "Delete",
  },
];
const testConnectorOverview: ConnectorOverview = {
  connectorInfo: {
    connectorId: 1,
    connectorStatus: "statusplaceholder",
    connectorName: "connector-test",
    runningTasks: 0,
    failedTasks: 0,
    environmentId: "4",
    teamName: "Ospo",
    teamId: 0,
    showEditConnector: true,
    showDeleteConnector: true,
    connectorDeletable: true,
    hasOpenRequest: false,
    highestEnv: false,
    connectorOwner: false,
    connectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "my-connector",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    environmentName: "DEV",
  },
  connectorHistoryList: testConnectorHistoryList,
  promotionDetails: {
    sourceEnv: "4",
    connectorName: "connector-test",
    targetEnvId: "6",
    sourceConnectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "my-connector",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    targetEnv: "ACC",
    status: "success",
  },
  connectorExists: true,
  availableEnvironments: [
    {
      id: "3",
      name: "DEV",
    },
    {
      id: "10",
      name: "ACC",
    },
  ],
  connectorIdForDocumentation: 1003,
};

jest.mock("src/app/features/connectors/details/ConnectorDetails");

const mockUseConnectorDetails = useConnectorDetails as jest.MockedFunction<
  typeof useConnectorDetails
>;

const columnsFieldMap = [
  { columnHeader: "Logs", relatedField: "remarks" },
  { columnHeader: "Team", relatedField: "teamName" },
  { columnHeader: "Requested by", relatedField: "requestedBy" },
  { columnHeader: "Requested on", relatedField: "requestedTime" },
  { columnHeader: "Approved by", relatedField: "approvedBy" },
  { columnHeader: "Approved on", relatedField: "approvedTime" },
];

describe("ConnectorHistory", () => {
  beforeAll(mockIntersectionObserver);

  describe("handles an empty history", () => {
    beforeAll(() => {
      mockUseConnectorDetails.mockReturnValue({
        connectorIsRefetching: false,
        environmentId: "1",
        connectorOverview: {
          ...testConnectorOverview,
          connectorHistoryList: [],
        },
      });

      customRender(<ConnectorHistory />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows the page header headline", () => {
      const headline = screen.getByRole("heading", {
        name: "History",
      });

      expect(headline).toBeVisible();
    });

    it("shows a headline informing user about missing history", () => {
      const headline = screen.getByRole("heading", {
        name: "No Connector history",
      });

      expect(headline).toBeVisible();
    });

    it("shows information about missing history", () => {
      const infoText = screen.getByText("This connector contains no history.");

      expect(infoText).toBeVisible();
    });

    it("does not render a table", () => {
      const table = screen.queryByRole("table");

      expect(table).not.toBeInTheDocument();
    });
  });

  describe("handles a loading state on former empty history", () => {
    beforeAll(() => {
      mockUseConnectorDetails.mockReturnValue({
        connectorIsRefetching: true,
        environmentId: "1",
        connectorOverview: {
          ...testConnectorOverview,
          connectorHistoryList: [],
        },
      });

      customRender(<ConnectorHistory />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a table with loading information for connectors history", () => {
      const loadingTable = screen.getByRole("table", {
        name: "Loading",
      });

      expect(loadingTable).toBeVisible();
    });

    it("shows all column headers", () => {
      const header = screen.getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows one row for loading animation plus header row", () => {
      const row = screen.getAllByRole("row");

      expect(row).toHaveLength(2);
    });
  });

  describe("shows a table with connectors history", () => {
    beforeAll(() => {
      mockUseConnectorDetails.mockReturnValue({
        connectorIsRefetching: false,
        environmentId: "1",
        connectorOverview: testConnectorOverview,
      });

      customRender(<ConnectorHistory />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows the page header headline", () => {
      const headline = screen.getByRole("heading", {
        name: "History",
      });

      expect(headline).toBeVisible();
    });

    it("shows a table for connectors history", () => {
      const table = screen.getByRole("table", {
        name: "Connector history",
      });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", {
        name: "Connector history",
      });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", {
        name: "Connector history",
      });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(testConnectorHistoryList.length + 1);
    });

    it(`renders the right amount of cells based in connectors history list`, () => {
      const table = screen.getByRole("table", {
        name: "Connector history",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * testConnectorHistoryList.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector history",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      testConnectorHistoryList.forEach((historyEntry, index) => {
        it(`shows field ${column.relatedField} for history entry number ${index}`, () => {
          const table = screen.getByRole("table", {
            name: "Connector history",
          });

          // eslint-disable-next-line @typescript-eslint/ban-ts-comment
          //@ts-ignore
          const field = historyEntry[column.relatedField];

          let text = field;
          if (
            column.columnHeader === "Requested on" ||
            column.columnHeader === "Approved on"
          ) {
            text = `${field}${"\u00A0"}UTC`;
          }

          const cell = within(table).getByRole("cell", { name: text });

          expect(cell).toBeVisible();
        });
      });
    });
  });

  describe("handles a loading state for updating existing data", () => {
    beforeAll(() => {
      mockUseConnectorDetails.mockReturnValue({
        connectorIsRefetching: true,
        environmentId: "1",
        connectorOverview: testConnectorOverview,
      });

      customRender(<ConnectorHistory />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a table with loading information for connectors history", () => {
      const loadingTable = screen.getByRole("table", {
        name: "Loading",
      });

      expect(loadingTable).toBeVisible();
    });

    it("shows all column headers", () => {
      const header = screen.getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows one row per entry with animation plus header row", () => {
      const row = screen.getAllByRole("row");

      expect(row).toHaveLength(testConnectorHistoryList.length + 1);
    });
  });
});
