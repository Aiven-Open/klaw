import {
  screen,
  within,
  cleanup,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import Topics from "src/app/pages/topics";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseSinglePage,
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";
import { mockGetEnvironments } from "src/domain/environment";
import { mockGetTeams } from "src/domain/team/team-api.msw";

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
        response: { status: 200, data: mockedResponseSinglePage },
      });
      customRender(<Topics />, { memoryRouter: true, queryClient: true });
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
