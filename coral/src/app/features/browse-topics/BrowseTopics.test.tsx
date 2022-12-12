import { server } from "src/services/api-mocks/server";
import {
  mockedResponseMultiplePage,
  mockedResponseSinglePage,
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, within, screen } from "@testing-library/react";
import BrowseTopics from "src/app/features/browse-topics/BrowseTopics";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { mockGetEnvironments } from "src/domain/environment";
import { mockedTeamResponse, mockGetTeams } from "src/domain/team/team-api.msw";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),
    Icon: jest.fn(),
  };
});

describe("BrowseTopics.tsx", () => {
  beforeAll(() => {
    server.listen();
    mockIntersectionObserver();
  });

  afterAll(() => {
    server.close();
  });

  describe("handles loading state", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 200, data: mockedResponseSinglePage },
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a loading message while data is being fetched", () => {
      const loading = screen.getByText("Loading...");

      expect(loading).toBeVisible();
    });
  });

  describe("handles error responses", () => {
    beforeEach(() => {
      console.error = jest.fn();
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 400, data: { message: "Not relevant" } },
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      jest.restoreAllMocks();
      server.resetHandlers();
      cleanup();
    });

    it("shows an error message to the user", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const errorMessage = screen.getByText("Something went wrong 😔");

      expect(errorMessage).toBeVisible();
    });
  });

  describe("handles an empty response", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 200, data: [] },
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows an info to user that no topic is found", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const emptyMessage = screen.getByText("No topics found");

      expect(emptyMessage).toBeVisible();
    });
  });

  describe("handles successful response with one page", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 200, data: mockedResponseSinglePage },
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("renders a select element to filter topics by Kafka environment", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a select element to filter topics by team", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Team",
      });

      expect(select).toBeEnabled();
    });

    it("renders the topic table with information about the pages", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      const table = screen.getByRole("table", {
        name: `Topics overview, page 1 of 1`,
      });

      expect(table).toBeVisible();
    });

    it("shows topic names row headers", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      const table = screen.getByRole("table", {
        name: "Topics overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("rowheader", {
        name: mockedResponseTransformed.entries[0].topicName,
      });
      expect(rowHeader).toBeVisible();
    });

    it("does not render the pagination", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const pagination = screen.queryByRole("navigation", {
        name: "Pagination",
      });

      expect(pagination).not.toBeInTheDocument();
    });
  });

  describe("handles successful response with 4 pages", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: mockedResponseMultiplePage },
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("renders the topic table with information about the pages", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const pagination = screen.getByRole("table", {
        name: "Topics overview, page 2 of 4",
      });

      expect(pagination).toBeVisible();
    });
    it("shows a pagination", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const pagination = screen.getByRole("navigation", {
        name: "Pagination",
      });

      expect(pagination).toBeVisible();
    });

    it("shows page 2 as currently active page and the total page number", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const activePageInformation = screen.getByText("You are on page 2 of 4");

      expect(activePageInformation).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({ mswInstance: server });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows page 1 as currently active page and the total page number", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      const activePageInformation = screen.getByText("You are on page 1 of 10");

      expect(activePageInformation).toBeVisible();
    });

    it("fetches new data when user clicks on next page", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 2",
      });

      await userEvent.click(pageTwoButton);

      const activePageInformation = await screen.findByText(
        "You are on page 2 of 10"
      );
      expect(activePageInformation).toBeVisible();
    });
  });

  describe("handles user filtering topics by environment", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({ mswInstance: server });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a select element for environments with `ALL` preselected", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Environment",
      });

      expect(select).toHaveValue("ALL");
    });

    it("shows an information that the list is updated after user selected an environment", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Environment",
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      const updatingList = screen.getByText("Filtering list...");
      expect(updatingList).toBeVisible();
    });

    it("changes active selected option when user selects `DEV`", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Environment",
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("1");
    });

    it("fetches new data when user selects `DEV`", async () => {
      const getAllTopics = () =>
        within(
          screen.getByRole("table", { name: /Topics overview/ })
        ).getAllByRole("rowheader");
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      expect(getAllTopics()).toHaveLength(10);

      const select = screen.getByRole("combobox", {
        name: "Filter By Environment",
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });

      await userEvent.selectOptions(select, option);
      await waitForElementToBeRemoved(screen.getByText("Filtering list..."));

      expect(getAllTopics()).toHaveLength(3);
    });
  });

  describe("handles user filtering topics by team", () => {
    beforeEach(() => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });
      mockTopicGetRequest({ mswInstance: server });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a select element for team with `All teams` preselected", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Team",
      });

      expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");
    });

    it("changes active selected option when user selects `TEST_TEAM_02`", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Filter By Team",
      });
      const option = within(select).getByRole("option", {
        name: "TEST_TEAM_02",
      });
      expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("TEST_TEAM_02");
    });

    it("fetches new data when user selects `TEST_TEAM_02`", async () => {
      const getAllTopics = () =>
        within(
          screen.getByRole("table", { name: /Topics overview/ })
        ).getAllByRole("rowheader");
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      expect(getAllTopics()).toHaveLength(10);

      const select = screen.getByRole("combobox", {
        name: "Filter By Team",
      });
      const option = within(select).getByRole("option", {
        name: "TEST_TEAM_02",
      });

      await userEvent.selectOptions(select, option);
      await waitForElementToBeRemoved(screen.getByText("Filtering list..."));

      expect(getAllTopics()).toHaveLength(2);
    });
  });
});
