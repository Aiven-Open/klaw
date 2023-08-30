import { cleanup, screen, waitFor } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import { ConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorOverview, getConnectorOverview } from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";

const mockMatches = jest.fn();
const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () =>
    jest.fn().mockReturnValue({
      connectornamesearch: testConnectorName,
    }),
  useMatches: () => mockMatches(),
  Navigate: () => mockedNavigate(),
}));

jest.mock("src/domain/connector/connector-api");

const mockGetConnectorOverview = getConnectorOverview as jest.MockedFunction<
  typeof getConnectorOverview
>;

const testConnectorName = "my-connector";
const testConnectorOverview: ConnectorOverview = {
  connectorInfo: {
    connectorId: 1,
    connectorStatus: "statusplaceholder",
    connectorName: testConnectorName,
    runningTasks: 0,
    failedTasks: 0,
    environmentId: "4",
    teamName: "Ospo",
    teamId: 0,
    showEditConnector: true,
    showDeleteConnector: true,
    connectorDeletable: true,
    hasOpenRequest: false,
    hasOpenClaimRequest: false,
    highestEnv: false,
    connectorOwner: false,
    connectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "my-connector",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    environmentName: "DEV",
  },
  connectorHistoryList: [],
  promotionDetails: {
    sourceEnv: "4",
    connectorName: testConnectorName,
    targetEnvId: "6",
    sourceConnectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "my-connector",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    targetEnv: "ACC",
    status: "SUCCESS",
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

describe("ConnectorDetails", () => {
  beforeAll(() => {
    mockedNavigate.mockImplementation(() => {
      return <div data-testid={"react-router-navigate"} />;
    });
  });

  describe("fetches the connector overview based on connector name", () => {
    beforeAll(() => {
      mockGetConnectorOverview.mockResolvedValue(testConnectorOverview);
      mockMatches.mockImplementation(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches connector overview and schema data on first load of page", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitFor(() =>
        expect(mockGetConnectorOverview).toHaveBeenCalledWith({
          connectornamesearch: testConnectorName,
          environmentId: testConnectorOverview.availableEnvironments[0].id,
        })
      );
    });

    it("fetches the data anew when user changes environment", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const select = await screen.findByRole("combobox", {
        name: "Select environment",
      });

      await userEvent.selectOptions(
        select,
        testConnectorOverview.availableEnvironments[1].name
      );

      await waitFor(() =>
        expect(mockGetConnectorOverview).toHaveBeenCalledWith({
          connectornamesearch: testConnectorName,
          environmentId: testConnectorOverview.availableEnvironments[1].id,
        })
      );
    });
  });

  describe("renders the correct tab navigation based on router match", () => {
    beforeEach(() => {
      mockGetConnectorOverview.mockResolvedValue(testConnectorOverview);
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows the tablist with Overview as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("Overview");
    });

    it("shows the tablist with History as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_history",
        },
      ]);

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("History");
    });
  });

  describe("only renders header and tablist if route is matching defined tabs", () => {
    beforeEach(() => {
      mockGetConnectorOverview.mockResolvedValue(testConnectorOverview);
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("does render content if the route matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.getByRole("tablist");

      expect(tabList).toBeVisible();
      expect(mockedNavigate).not.toHaveBeenCalled();
    });

    it("redirects user to connector overview if the route does not matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "something",
        },
      ]);

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.queryByRole("tablist");

      expect(tabList).not.toBeInTheDocument();

      expect(mockedNavigate).toHaveBeenCalled();
    });
  });
});
