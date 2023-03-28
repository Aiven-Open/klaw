import {
  cleanup,
  screen,
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

const mockedNavigator = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigator,
}));

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
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "All topics",
      });

      expect(headline).toBeVisible();
    });

    it("renders 'Request new topic' button in heading", async () => {
      const button = screen.getByRole("button", {
        name: "Request new topic",
      });

      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("navigates to '/requestTopics' when user clicks the button 'Request new topic'", async () => {
      const button = screen.getByRole("button", {
        name: "Request new topic",
      });

      await userEvent.click(button);

      expect(mockedNavigator).toHaveBeenCalledWith("/topics/request");
    });

    it("navigates to '/requestTopics' when user presses Enter while 'Request new topic' button is focused", async () => {
      const button = screen.getByRole("button", {
        name: "Request new topic",
      });

      await tabNavigateTo({ targetElement: button });

      await userEvent.keyboard("{Enter}");

      expect(mockedNavigator).toHaveBeenCalledWith("/topics/request");
    });

    it("shows a table with topics", async () => {
      const table = screen.getByRole("table", { name: /Topics overview/ });

      expect(table).toBeVisible();
    });

    it("shows a table row for each topic", () => {
      const table = screen.getByRole("table", { name: /Topics overview/ });
      const row = within(table).getAllByRole("row");

      // Adding one row for the table headers
      expect(row).toHaveLength(mockedResponseTransformed.entries.length + 1);
    });
  });
});
