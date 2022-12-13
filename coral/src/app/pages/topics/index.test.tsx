import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import Topics from "src/app/pages/topics";
import { mockGetEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { mockedTeamResponse, mockGetTeams } from "src/domain/team/team-api.msw";
import {
  mockedResponseSinglePage,
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";
import { server } from "src/services/api-mocks/server";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

const { location } = window;

describe("Topics", () => {
  beforeAll(() => {
    server.listen();
    mockIntersectionObserver();

    // This TS disabling is because of "The operand of a 'delete' operator must be optional." TS error.
    // But this delete is necessary to correctly mock window.location
    // Without it, we get "Error: Not implemented: navigation (except hash changes)" in JSDOM
    // Sources:
    //  https://stackoverflow.com/questions/54090231/how-to-fix-error-not-implemented-navigation-except-hash-changes
    //  https://remarkablemark.org/blog/2021/04/14/jest-mock-window-location-href/
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    delete window.location;
    window.location = {
      href: "/topics",
    } as Location;
  });

  afterAll(() => {
    server.close();

    window.location = location;
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

    it("renders 'Request A New Topic' button in heading", async () => {
      const button = screen.getByRole("button", {
        name: "Request A New Topic",
      });

      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("navigates to '/requestTopics' when user clicks the button 'Request A New Topic'", async () => {
      const button = screen.getByRole("button", {
        name: "Request A New Topic",
      });

      await userEvent.click(button);

      await waitFor(() => expect(window.location.href).toBe("/requestTopics"));
    });

    it("navigates to '/requestTopics' when user presses Enter while 'Request A New Topic' button is focused", async () => {
      const button = screen.getByRole("button", {
        name: "Request A New Topic",
      });

      await tabNavigateTo({ targetElement: button });

      await userEvent.keyboard("{Enter}");

      await waitFor(() => expect(window.location.href).toBe("/requestTopics"));
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
