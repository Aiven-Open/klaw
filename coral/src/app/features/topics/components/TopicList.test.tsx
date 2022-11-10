// @TODO import from "@testing-library/react/pure" disables auto cleanup, remove when main is merged
import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import { server } from "src/services/api-mocks/server";
import { renderWithQueryClient } from "src/services/test-utils";
import { TopicList } from "src/app/features/topics/components/TopicList";
import {
  mockedResponse,
  mockTopicGetRequest,
} from "src/domain/topics/topics-api.msw";

// This mirrors the formatting formation used in `/domain`
// it's a temp implementation here and will be removed
// as soon as we have the final API schema
const allTopiNamesFromApi = mockedResponse
  .flat()
  .map((topic) => topic.topicName);

describe("TopicList.tsx", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("handles loading state", () => {
    beforeEach(() => {
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<TopicList />);
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
      renderWithQueryClient(<TopicList />);
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
      renderWithQueryClient(<TopicList />);
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

  describe("handles successful response", () => {
    beforeEach(() => {
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<TopicList />);
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a list of all topics", async () => {
      await waitForElementToBeRemoved(screen.getByText("Loading..."));

      allTopiNamesFromApi.forEach((name) => {
        const topicCard = screen.getByText(name);

        expect(topicCard).toBeVisible();
      });
    });
  });
});
