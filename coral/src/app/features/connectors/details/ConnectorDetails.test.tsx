import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import { ConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import {
  ConnectorOverview,
  getConnectorOverview,
  requestConnectorClaim,
} from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";

const mockMatches = jest.fn();
const mockedNavigate = jest.fn();
const mockedUseToast = jest.fn();
const mockGetConnectorOverview = getConnectorOverview as jest.MockedFunction<
  typeof getConnectorOverview
>;

const mockRequestConnectorClaim = requestConnectorClaim as jest.MockedFunction<
  typeof requestConnectorClaim
>;

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

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

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
    hasOpenRequestOnAnyEnv: false,
    connectorOwner: true,
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
  describe("fetches the connector overview based on connector name", () => {
    beforeEach(() => {
      mockGetConnectorOverview.mockResolvedValue(testConnectorOverview);
      mockMatches.mockImplementation(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches connector overview and schema data on first load of page", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });
      expect(mockGetConnectorOverview).toHaveBeenNthCalledWith(1, {
        connectornamesearch: testConnectorName,
        environmentId: undefined,
      });
    });

    it("updates the environment based on lowest environment of the connector", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitFor(() => {
        expect(mockGetConnectorOverview).toHaveBeenNthCalledWith(2, {
          connectornamesearch: testConnectorName,
          environmentId: testConnectorOverview.availableEnvironments[0].id,
        });
      });
    });

    it("fetches the data anew when user changes environment", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const select = await screen.findByRole("combobox", {
        name: "Select environment",
      });

      await userEvent.selectOptions(
        select,
        testConnectorOverview.availableEnvironments[1].name
      );

      await waitFor(() =>
        expect(mockGetConnectorOverview).toHaveBeenNthCalledWith(3, {
          connectornamesearch: testConnectorName,
          environmentId: testConnectorOverview.availableEnvironments[1].id,
        })
      );
    });

    it("fetches the correct data when URL has env search param", async () => {
      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: "/?env=10",
      });

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

    afterEach(cleanup);

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

    afterEach(cleanup);

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

  describe("correctly renders the Claim topic banner", () => {
    beforeEach(() => {
      mockMatches.mockImplementation(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
    });

    afterEach(cleanup);

    it("does not renders the Claim connector banner when user is topic owner", async () => {
      mockGetConnectorOverview.mockResolvedValue(testConnectorOverview);

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const description = screen.queryByText(
        "Your team is not the owner of this connector. Click below to create a claim request for this connector."
      );
      const button = screen.queryByRole("button", { name: "Claim connector" });

      expect(description).not.toBeInTheDocument();
      expect(button).not.toBeInTheDocument();
    });

    it("renders the Claim connector banner when user is not topic owner", async () => {
      mockGetConnectorOverview.mockResolvedValue({
        ...testConnectorOverview,
        connectorInfo: {
          ...testConnectorOverview.connectorInfo,
          connectorOwner: false,
        },
      });

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const description = await waitFor(() =>
        screen.getByText(
          `This connector is currently owned by ${testConnectorOverview.connectorInfo.teamName}. Select "Claim connector" to request ownership.`
        )
      );
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      expect(description).toBeVisible();
      expect(button).toBeEnabled();
    });
  });

  describe("allows users to claim a connector when they are not connector owner", () => {
    const originalConsoleError = console.error;

    beforeEach(async () => {
      console.error = jest.fn();
      mockMatches.mockImplementation(() => [
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
      mockGetConnectorOverview.mockResolvedValue({
        ...testConnectorOverview,
        connectorInfo: {
          ...testConnectorOverview.connectorInfo,
          connectorOwner: false,
        },
      });

      customRender(<ConnectorDetails connectorName={testConnectorName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a modal when clicking the Claim connector button", async () => {
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog", { name: "Claim connector" });

      expect(modal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("sends a request when clicking the submit button without entering a message", async () => {
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog", { name: "Claim connector" });

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim connector",
      });

      await userEvent.click(submitButton);

      expect(mockRequestConnectorClaim).toHaveBeenCalledWith({
        connectorName: testConnectorOverview.connectorInfo.connectorName,
        env: testConnectorOverview.connectorInfo.environmentId,
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("closes the modal and renders a toast when request was successful", async () => {
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog", { name: "Claim connector" });

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim connector",
      });

      await userEvent.click(submitButton);

      expect(modal).not.toBeVisible();

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith({
          message: "Connector claim request successfully created",
          position: "bottom-left",
          variant: "default",
        })
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("refetches connector overview after successful request", async () => {
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );
      await userEvent.click(button);
      const submitButton = screen.getByRole("button", {
        name: "Request claim connector",
      });

      await userEvent.click(submitButton);

      await waitFor(() => {
        // 1. api call inital page load
        // 2. api call for updating environment
        expect(mockGetConnectorOverview).toHaveBeenNthCalledWith(3, {
          connectornamesearch: "my-connector",
          environmentId: "3",
        });
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("sends a request when clicking the submit button with the message entered by the user", async () => {
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog", { name: "Claim connector" });

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim connector",
      });

      const textArea = screen.getByRole("textbox");

      await userEvent.type(textArea, "hello");
      await userEvent.click(submitButton);

      expect(mockRequestConnectorClaim).toHaveBeenCalledWith({
        connectorName: testConnectorOverview.connectorInfo.connectorName,
        env: testConnectorOverview.connectorInfo.environmentId,
        remark: "hello",
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("closes the modal displays an alert when request was not successful", async () => {
      const mockErrorMessage = "There was an error";
      mockRequestConnectorClaim.mockRejectedValue(mockErrorMessage);

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim connector" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog", { name: "Claim connector" });

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim connector",
      });

      await userEvent.click(submitButton);

      expect(modal).not.toBeVisible();

      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
      expect(console.error).toHaveBeenCalledWith(mockErrorMessage);
    });
  });
});
