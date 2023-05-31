import { cleanup, screen } from "@testing-library/react";
import { TopicDetailsPage } from "src/app/pages/topics/details";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getTopicOverview } from "src/domain/topic/topic-api";

const testTopic = "my-nice-topic";
const mockedUsedNavigate = jest.fn();
const mockUseParams = jest.fn();
const mockMatches = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => mockUseParams(),
  useMatches: () => mockMatches(),
  useNavigate: () => mockedUsedNavigate,
}));

jest.mock("src/domain/topic/topic-api");
const mockGetTopicOverview = getTopicOverview as jest.MockedFunction<
  typeof getTopicOverview
>;
describe("TopicOverviewPage", () => {
  describe("renders the component handling header and tabs for topic-overview", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      mockGetTopicOverview.mockReturnValue({});
      mockUseParams.mockReturnValue({
        topicName: testTopic,
      });

      mockMatches.mockReturnValue([
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicDetailsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("renders the right headline with the prop passed form url", () => {
      const text = screen.getByRole("heading", { name: testTopic });

      expect(text).toBeVisible();
    });

    it("does not redirect", () => {
      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });
  });

  describe("redirects user if there is no topicName param", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      mockGetTopicOverview.mockReturnValue({});
      mockUseParams.mockImplementationOnce(() => {
        return {};
      });
      customRender(<TopicDetailsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("does not render the topics name", () => {
      const headline = screen.queryByRole("heading");

      expect(headline).not.toBeInTheDocument();
    });

    it("does redirect user to /topics", () => {
      expect(mockedUsedNavigate).toHaveBeenCalledWith("/topics");
    });
  });
});
