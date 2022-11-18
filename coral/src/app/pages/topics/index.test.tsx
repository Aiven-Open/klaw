import { screen, within } from "@testing-library/react/pure";
import Topics from "src/app/pages/topics";
import { renderWithQueryClient } from "src/services/test-utils";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseTransformed,
  mockGetTeams,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";
import {
  cleanup,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import { mockGetEnvironments } from "src/domain/environment";

// This mirrors the formatting formation used in `/domain`
// it's a temp implementation here and will be removed
// as soon as we have the final API schema

describe("Topics", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("renders default view with data from API", () => {
    beforeAll(async () => {
      mockGetEnvironments({ mswInstance: server });
      mockGetTeams({ mswInstance: server });
      mockTopicGetRequest({
        mswInstance: server,
        scenario: "single-page-static",
      });
      renderWithQueryClient(<Topics />);
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterAll(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Browse all topics",
      });

      expect(headline).toBeVisible();
    });

    it("renders a select element to filter topics by Kafka environment", async () => {
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a select element to filter topics by team", async () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      expect(select).toBeEnabled();
    });

    it("shows a list of topics", async () => {
      const list = screen.getByRole("list", { name: "Topics" });

      expect(list).toBeVisible();
    });

    it("shows list items for each topic", () => {
      const list = screen.getByRole("list", { name: "Topics" });
      const listItem = within(list).getAllByRole("listitem");

      expect(listItem).toHaveLength(mockedResponseTransformed.entries.length);
    });
  });
});
