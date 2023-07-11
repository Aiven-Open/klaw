import { cleanup, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { ConnectorOverviewResourcesTabs } from "src/app/features/connectors/details/components/ConnectorOverviewResourcesTabs";
import { ConnectorOverviewTabEnum } from "src/app/router_utils";
import { ConnectorOverview } from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const testMapTabs = [
  {
    linkTo: "overview",
    title: "Overview",
  },
  {
    linkTo: "documentation",
    title: "Documentation",
  },
  {
    linkTo: "history",
    title: "History",
  },
];

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
    connectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "my-connector",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    environmentName: "DEV",
  },
  topicHistoryList: [],
  promotionDetails: {
    sourceEnv: "4",
    connectorName: testConnectorName,
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
const defaultProps = {
  currentTab: ConnectorOverviewTabEnum.OVERVIEW,
  environmentId: "1",
  isError: false,
  isLoading: false,
  topicName: testConnectorName,
  connectorOverview: testConnectorOverview,
  connectorIsRefetching: false,
};

describe("ConnectorOverviewResourceTabs", () => {
  const user = userEvent.setup();

  describe("handles error state", () => {
    beforeAll(() => {
      customRender(
        <ConnectorOverviewResourcesTabs {...defaultProps} isError={true} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows an alert", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
    });

    it("shows an error message", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toHaveTextContent(
        "There was an error trying to load the connector details"
      );
    });

    it("does not show content for active tab navigation", () => {
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("handles loading state", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a loading information (initial load)", () => {
      customRender(
        <ConnectorOverviewResourcesTabs {...defaultProps} isLoading={true} />,
        { queryClient: true, memoryRouter: true }
      );
      const loading = screen.getByText("Loading connector details");

      expect(loading).toBeVisible();
    });

    it("shows a loading information (refetching)", () => {
      customRender(
        <ConnectorOverviewResourcesTabs
          {...defaultProps}
          isLoading={false}
          connectorIsRefetching={true}
        />,
        { queryClient: true, memoryRouter: true }
      );
      const loading = screen.getByText("Loading connector details");

      expect(loading).toBeVisible();
    });

    it("does not show content for active tab navigation", () => {
      customRender(
        <ConnectorOverviewResourcesTabs {...defaultProps} isLoading={true} />,
        { queryClient: true, memoryRouter: true }
      );
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("handles a non-existent connector", () => {
    beforeAll(() => {
      customRender(
        <ConnectorOverviewResourcesTabs
          {...defaultProps}
          connectorOverview={{
            ...testConnectorOverview,
            connectorExists: false,
          }}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows an alert", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
    });

    it("shows an information about non-existing connector", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toHaveTextContent(
        `Connector ${testConnectorName} does not exist.`
      );
    });

    it("does not show content for active tab navigation", () => {
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("renders the detail page for connector", () => {
    beforeAll(() => {
      customRender(
        <ConnectorOverviewResourcesTabs
          {...defaultProps}
          currentTab={ConnectorOverviewTabEnum.OVERVIEW}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders a tablist", () => {
      const tablistWrapper = screen.getByTestId("tabpanel-content");
      const tablist = screen.getByRole("tablist");

      expect(tablistWrapper).toBeVisible();
      expect(tablist).toBeVisible();
    });

    testMapTabs.forEach((tab) => {
      const name = tab.title;
      it(`shows a tab element "${name}"`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab).toBeVisible();
      });

      it(`renders a button as the "${name}" tab element`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab.tagName).toBe("BUTTON");
      });

      it(`adds information which part of the panel the button "${name}" controls`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab).toHaveAttribute(
          "aria-controls",
          `${name.toLowerCase()}-panel`
        );
      });
    });

    it("shows which tab is currently selected based on a prop", () => {
      const tab = screen.getByRole("tab", { selected: true });

      expect(tab).toHaveAccessibleName("Overview");
    });

    it("shows a preview banner to enables users to go back to original app", () => {
      const banner = screen.getByRole("region", { name: "Preview disclaimer" });
      const link = within(banner).getByRole("link", { name: "old interface" });

      expect(banner).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        `/connectorOverview?connectorName=${testConnectorName}`
      );
    });
  });

  describe("enables users to switch panels", () => {
    beforeEach(() => {
      customRender(
        <ConnectorOverviewResourcesTabs
          {...defaultProps}
          currentTab={ConnectorOverviewTabEnum.OVERVIEW}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    testMapTabs.forEach((tab) => {
      const name = tab.title;
      const linkTo = tab.linkTo;

      it(`navigates to correct URL when ${name} tab is clicked`, async () => {
        const tab = screen.getByRole("tab", { name: name });
        await user.click(tab);
        expect(mockedNavigate).toHaveBeenCalledWith(
          `/connector/${testConnectorName}/${linkTo}`,
          {
            replace: true,
          }
        );
      });
    });
  });
});
