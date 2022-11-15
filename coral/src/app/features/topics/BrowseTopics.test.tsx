import { cleanup, screen } from "@testing-library/react";
import { server } from "src/services/api-mocks/server";
import { renderWithQueryClient } from "src/services/test-utils";
import {
  mockTopicGetRequest,
  mockedResponseTransformed,
} from "src/domain/topics/topics-api.msw";
import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { waitForElementToBeRemoved, within } from "@testing-library/react/pure";

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
      mockTopicGetRequest({ mswInstance: server });
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
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<BrowseTopics />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
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
      mockTopicGetRequest({ mswInstance: server, scenario: "multiple-pages" });
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
});
