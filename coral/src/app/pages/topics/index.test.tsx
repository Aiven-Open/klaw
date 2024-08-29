import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react/pure";
import { userEvent } from "@testing-library/user-event";
import Topics from "src/app/pages/topics";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { getTeams } from "src/domain/team";
import { getTopics } from "src/domain/topic";
import { mockedResponseTransformed } from "src/domain/topic/topic-test-helper";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";
import { UseAuthContext } from "src/app/context-provider/AuthProvider";

const mockedNavigator = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigator,
}));

let mockAuthUserContext: UseAuthContext = {
  ...testAuthUser,
  isSuperAdminUser: false,
};
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUserContext,
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

  describe("renders default view with data from API for users", () => {
    beforeAll(async () => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: false };
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockGetTopicsResponse);

      customRender(<Topics />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
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

    it("navigates to '/topics/request' when user clicks the button 'Request new topic'", async () => {
      const button = screen.getByRole("button", {
        name: "Request new topic",
      });

      await userEvent.click(button);

      expect(mockedNavigator).toHaveBeenCalledWith("/topics/request");
    });

    it("navigates to '/topics/request' when user presses Enter while 'Request new topic' button is focused", async () => {
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

  describe("does not render the button to request a new topic for superadmin", () => {
    beforeAll(async () => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: true };
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockGetTopicsResponse);

      customRender(<Topics />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
    });

    it("shows the same headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Topics",
      });

      expect(headline).toBeVisible();
    });

    it("shows the table with topics", async () => {
      const table = screen.getByRole("table", { name: /Topics overview/ });

      expect(table).toBeVisible();
    });

    it("does not renders 'Request new topic' button", async () => {
      const button = screen.queryByText("Request new topic");

      expect(button).not.toBeInTheDocument();
    });
  });
});
