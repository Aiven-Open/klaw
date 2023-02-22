import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { mockedTeamResponse } from "src/domain/team/team-api.msw";
import {
  mockedResponseMultiplePageTransformed,
  mockedResponseTransformed,
} from "src/domain/topic/topic-api.msw";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { transformTeamNamesGetResponse } from "src/domain/team/team-transformer";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { getTeamNames, Team } from "src/domain/team";
import { getTopics } from "src/domain/topic";
import { Environment, getEnvironments } from "src/domain/environment";

jest.mock("src/domain/team/team-api.ts");
jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetTeams = getTeamNames as jest.MockedFunction<typeof getTeamNames>;
const mockGetTopics = getTopics as jest.MockedFunction<typeof getTopics>;
const mockGetEnvironments = getEnvironments as jest.MockedFunction<
  typeof getEnvironments
>;

const filterByEnvironmentLabel = "Filter by Environment";
const filterByTeamLabel = "Filter by team";

// @TODO find better location / handling for mocks
// depends on how we proceed
const mockedGetTopicsResponseSinglePage: TopicApiResponse =
  mockedResponseTransformed;
const mockedGetTopicsResponseMultiplePages: TopicApiResponse =
  mockedResponseMultiplePageTransformed;
const mockGetEnvironmentResponse: Environment[] =
  transformEnvironmentApiResponse(mockedEnvironmentResponse);
const mockGetTeamsResponse: Team[] =
  transformTeamNamesGetResponse(mockedTeamResponse);

describe("BrowseTopics.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles loading state", () => {
    beforeAll(() => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseSinglePage);
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a loading message while data is being fetched", () => {
      const loading = screen.getByText("Loading...");

      expect(loading).toBeVisible();
    });
  });

  describe("handles error responses", () => {
    const originalConsoleError = console.error;

    beforeAll(async () => {
      console.error = jest.fn();

      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockRejectedValue("This is an error message");

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterAll(() => {
      console.error = originalConsoleError;
      jest.clearAllMocks();
      cleanup();
    });

    it("shows an error message to the user", () => {
      const errorMessage = screen.getByText("Something went wrong 😔");

      expect(errorMessage).toBeVisible();
    });

    it("receives the right error", () => {
      expect(true).toBeTruthy();
      expect(console.error).toHaveBeenCalledTimes(1);
      expect(console.error).toHaveBeenCalledWith("This is an error message");
    });
  });

  describe("handles an empty response", () => {
    beforeEach(() => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue({
        totalPages: 0,
        currentPage: 0,
        entries: [],
      });
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows an info to user that no topic is found", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const emptyMessage = screen.getByText("No topics found");

      expect(emptyMessage).toBeVisible();
    });
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue(mockGetTeamsResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseSinglePage);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders a select element to filter topics by Kafka environment", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByEnvironmentLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a select element to filter topics by team", () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders the topic table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: `Topics overview, page 1 of 1`,
      });

      expect(table).toBeVisible();
    });

    it("shows topic names row headers", () => {
      const table = screen.getByRole("table", {
        name: "Topics overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("rowheader", {
        name: mockedResponseTransformed.entries[0].topicName,
      });
      expect(rowHeader).toBeVisible();
    });

    it("does not render the pagination", () => {
      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).not.toBeInTheDocument();
    });
  });

  describe("handles successful response with 4 pages", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseMultiplePages);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the topic table with information about the pages", () => {
      const table = screen.getByRole("table", {
        name: "Topics overview, page 2 of 4",
      });

      expect(table).toBeVisible();
    });

    it("shows a pagination", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toBeVisible();
    });

    it("shows page 2 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", { name: /Pagination/ });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 2 of 4"
      );
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseMultiplePages);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 2 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 2 of 4"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 3",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 3,
        environment: "ALL",
        searchTerm: undefined,
        teamName: "f5ed03b4-c0da-4b18-a534-c7e9a13d1342",
      });
    });
  });

  describe("handles user filtering topics by environment", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTopics.mockResolvedValue(mockedResponseTransformed);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a select element for environments with `ALL` preselected", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByEnvironmentLabel,
      });

      expect(select).toHaveValue("ALL");
    });

    it("shows an information that the list is updated after user selected an environment", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      // I'm not happy with that, but it' s the only way I found
      // to make sure there's a information about the list being
      // updated rendered for the user. awaiting for the userEvent
      // will end in a state where the new data has already
      // arrived and is rendered, to the updating info will be gone
      userEvent.selectOptions(select, option);

      const updatingList = await screen.findByText("Filtering list...");
      expect(updatingList).toBeVisible();
    });

    it("changes active selected option when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("1");
    });

    it("fetches new data when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });

      await userEvent.selectOptions(select, option);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "1",
        searchTerm: undefined,
        teamName: "f5ed03b4-c0da-4b18-a534-c7e9a13d1342",
      });
    });
  });

  describe("handles user filtering topics by team", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue(mockGetTeamsResponse);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedResponseTransformed);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a select element for team with `All teams` preselected", () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });

      expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");
    });

    it("changes active selected option when user selects `TEST_TEAM_02`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });

      const option = await screen.findByRole("option", {
        name: "TEST_TEAM_02",
      });

      expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("TEST_TEAM_02");
    });

    it("shows an information that the list is updated after user selected a team", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });
      const option = within(select).getByRole("option", {
        name: "TEST_TEAM_02",
      });
      expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");

      // I'm not happy with that, but it' s the only way I found
      // to make sure there's a information about the list being
      // updated rendered for the user. awaiting for the userEvent
      // will end in a state where the new data has already
      // arrived and is rendered, to the updating info will be gone
      userEvent.selectOptions(select, option);

      const updatingList = await screen.findByText("Filtering list...");
      expect(updatingList).toBeVisible();
    });

    it("fetches new data when user selects `TEST_TEAM_02`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });
      const option = within(select).getByRole("option", {
        name: "TEST_TEAM_02",
      });

      await userEvent.selectOptions(select, option);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "ALL",
        searchTerm: undefined,
        teamName: "TEST_TEAM_02",
      });
    });
  });

  describe("handles user searching by topic name with search input", () => {
    const testSearchInput = "Searched for topic";
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedResponseTransformed);
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows an information that the list is updated after user submits search", async () => {
      const input = screen.getByRole("searchbox", {
        name: "Search by topic name",
      });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });
      await userEvent.type(input, testSearchInput);
      // I'm not happy with that, but it' s the only way I found
      // to make sure there's a information about the list being
      // updated rendered for the user. awaiting for the userEvent
      // will end in a state where the new data has already
      // arrived and is rendered, to the updating info will be gone
      userEvent.click(submitButton);

      const updatingList = await screen.findByText("Filtering list...");
      expect(updatingList).toBeVisible();
    });

    it("fetches new data when user enters text in input and clicks the search button", async () => {
      const input = screen.getByRole("searchbox", {
        name: "Search by topic name",
      });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });

      expect(input).toHaveValue("");
      expect(submitButton).toBeEnabled();

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);

      await userEvent.click(submitButton);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "ALL",
        searchTerm: "Searched for topic",
        teamName: "f5ed03b4-c0da-4b18-a534-c7e9a13d1342",
      });
    });

    it("fetches new data when when user enters text in input and presses 'Enter'", async () => {
      const input = screen.getByRole("searchbox", {
        name: "Search by topic name",
      });
      expect(input).toHaveValue("");

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);

      await userEvent.keyboard("{Enter}");

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "ALL",
        searchTerm: "Searched for topic",
        teamName: "f5ed03b4-c0da-4b18-a534-c7e9a13d1342",
      });
    });

    it("can navigate to search input and submit button with keyboard", async () => {
      const input = screen.getByRole("searchbox", {
        name: "Search by topic name",
      });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });

      expect(input).toHaveValue("");
      expect(submitButton).toBeEnabled();

      await tabNavigateTo({ targetElement: input });

      expect(input).toHaveFocus();

      await userEvent.tab();

      expect(submitButton).toHaveFocus();
    });

    it("fetches new data when user enters text in input and presses 'Enter' on focused submit button", async () => {
      const input = screen.getByRole("searchbox", {
        name: "Search by topic name",
      });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });

      expect(input).toHaveValue("");
      expect(submitButton).toBeEnabled();

      await tabNavigateTo({ targetElement: input });

      expect(input).toHaveFocus();

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);

      await userEvent.tab();

      expect(submitButton).toHaveFocus();

      await userEvent.keyboard("{Enter}");

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "ALL",
        searchTerm: "Searched for topic",
        teamName: "f5ed03b4-c0da-4b18-a534-c7e9a13d1342",
      });
    });
  });
});
