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
import { mockedTeamResponse, mockGetTeams } from "src/domain/team/team-api.msw";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

describe("Topics", () => {
  beforeAll(() => {
    server.listen();
    mockIntersectionObserver();
  });

  afterAll(() => {
    server.close();
  });

  describe("renders default view with data from API", () => {
    beforeAll(async () => {
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
        name: "Filter By Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a select element to filter topics by team", async () => {
      const select = screen.getByRole("combobox", {
        name: "Filter By Team",
      });

      expect(select).toBeEnabled();
    });

    it("shows a table with topics", async () => {
      const table = screen.getByRole("table", { name: /Topics overview/ });

      expect(table).toBeVisible();
    });

    it("shows a table row for each topic", () => {
      const table = screen.getByRole("table", { name: /Topics overview/ });
      const row = within(table).getAllByRole("rowheader");

      expect(row).toHaveLength(mockedResponseTransformed.entries.length);
    });
  });
});
