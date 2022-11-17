import { cleanup, screen } from "@testing-library/react";
import { server } from "src/services/api-mocks/server";
import { renderWithQueryClient } from "src/services/test-utils";
import {
  mockTopicGetRequest,
  mockedResponseTransformed,
  mockGetEnvs,
  mockGetTeams,
} from "src/domain/topics/topics-api.msw";
import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { waitForElementToBeRemoved, within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { TopicEnv } from "src/domain/topics";

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
  });

  afterAll(() => {
    server.close();
  });

  describe("handles loading state", () => {
    beforeEach(() => {
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({
        mswInstance: server,
        scenario: "single-page-static",
      });
      renderWithQueryClient(<BrowseTopics />);
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
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({ mswInstance: server, scenario: "error" });
      renderWithQueryClient(<BrowseTopics />);
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
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({ mswInstance: server, scenario: "empty" });
      renderWithQueryClient(<BrowseTopics />);
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
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({
        mswInstance: server,
        scenario: "single-page-static",
      });
      renderWithQueryClient(<BrowseTopics />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("renders a select element to filter topics by Kafka environment", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a select element to filter topics by team", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      expect(select).toBeEnabled();
    });

    it("renders the topic list", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const list = screen.getByRole("list", { name: "Topics" });

      expect(list).toBeVisible();
    });

    it("shows topic names as list item", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      const list = screen.getByRole("list", { name: "Topics" });

      const topicCard = within(list).getByText(
        mockedResponseTransformed.entries[0].topicName
      );
      expect(topicCard).toBeVisible();
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
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({
        mswInstance: server,
        scenario: "multiple-pages-static",
      });
      renderWithQueryClient(<BrowseTopics />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
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
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<BrowseTopics />);
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

  describe("handles user filtering topics by env", () => {
    beforeEach(() => {
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<BrowseTopics />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a select element for envs with `ALL` preselected", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });

      expect(select).toHaveValue(TopicEnv.ALL);
    });

    it("shows an information that the list is updated after user selected an env", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });
      const option = within(select).getByRole("option", {
        name: TopicEnv.DEV,
      });
      expect(select).toHaveValue(TopicEnv.ALL);

      await userEvent.selectOptions(select, option);

      const updatingList = screen.getByText("Filtering list...");
      expect(updatingList).toBeVisible();
    });

    it("changes active selected option when user selects `DEV`", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });
      const option = within(select).getByRole("option", {
        name: TopicEnv.DEV,
      });
      expect(select).toHaveValue(TopicEnv.ALL);

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(TopicEnv.DEV);
    });

    it("fetches new data when user selects `DEV`", async () => {
      const getAllTopics = () =>
        within(screen.getByRole("list", { name: "Topics" })).getAllByRole(
          "heading"
        );
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      expect(getAllTopics()).toHaveLength(10);

      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });
      const option = within(select).getByRole("option", {
        name: TopicEnv.DEV,
      });

      await userEvent.selectOptions(select, option);
      await waitForElementToBeRemoved(screen.getByText("Filtering list..."));

      expect(getAllTopics()).toHaveLength(3);
    });
  });

  describe("handles user filtering topics by team", () => {
    beforeEach(() => {
      mockGetEnvs({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<BrowseTopics />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a select element for team with `All teams` preselected", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      expect(select).toHaveValue("All teams");
    });

    it("changes active selected option when user selects `TEST_TEAM_02`", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
      const select = screen.getByRole("combobox", {
        name: "Team",
      });
      const option = within(select).getByRole("option", {
        name: "TEST_TEAM_02",
      });
      expect(select).toHaveValue("All teams");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("TEST_TEAM_02");
    });

    it("fetches new data when user selects `TEST_TEAM_02`", async () => {
      const getAllTopics = () =>
        within(screen.getByRole("list", { name: "Topics" })).getAllByRole(
          "heading"
        );
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      expect(getAllTopics()).toHaveLength(10);

      const select = screen.getByRole("combobox", {
        name: "Team",
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
