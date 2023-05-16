import { cleanup, screen } from "@testing-library/react";
import { TopicOverviewPage } from "src/app/pages/topics/overview";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const testTopic = "my-nice-topic";
const mockedUsedNavigate = jest.fn();
const mockUseParams = jest.fn();
const mockMatches = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => mockUseParams,
  useNavigate: () => mockedUsedNavigate,
  useMatches: () => mockMatches,
}));

describe("TopicOverviewPage", () => {
  describe("renders the component handling header and tabs for topic-overview", () => {
    beforeAll(() => {
      mockUseParams.mockImplementationOnce(() => {
        return {
          topicName: testTopic,
        };
      });

      mockMatches.mockImplementationOnce(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicOverviewPage />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
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
      customRender(<TopicOverviewPage />, {
        memoryRouter: true,
      });
    });

    afterAll(() => {
      jest.clearAllMocks();
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
