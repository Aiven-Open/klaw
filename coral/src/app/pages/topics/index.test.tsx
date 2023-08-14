import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import Topics from "src/app/pages/topics";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { getTeams } from "src/domain/team";
import { getTopics } from "src/domain/topic";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { mockedResponseTransformed } from "src/domain/topic/topic-api.msw";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedNavigator = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigator,
}));

jest.mock("src/domain/team/team-api.ts");
jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;
const mockGetTopics = getTopics as jest.MockedFunction<typeof getTopics>;
const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetTopicsResponse: TopicApiResponse = mockedResponseTransformed;

describe("Topics", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("renders default view with data from API", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockGetTopicsResponse);

      customRender(<Topics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
    });

    it("shows a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Topics",
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
