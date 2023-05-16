import { cleanup, screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";
import { within } from "@testing-library/react/pure";

const mockUseParams = jest.fn();
const mockMatches = jest.fn();
const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => mockUseParams(),
  useMatches: () => mockMatches(),
  Navigate: () => mockedNavigate(),
}));

describe("TopicDetails", () => {
  const testTopic = "my-nice-topic";

  beforeEach(() => {
    mockUseParams.mockReturnValue({
      topicName: testTopic,
    });

    mockedNavigate.mockImplementation(() => {
      return <div data-testid={"react-router-navigate"} />;
    });
  });

  describe("renders the correct tab navigation based on router match", () => {
    afterEach(cleanup);

    it("shows the tablist with Overview as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicDetails topicName={testTopic} />, {
        memoryRouter: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("Overview");
    });

    it("shows the tablist with History as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_history",
        },
      ]);

      customRender(<TopicDetails topicName={testTopic} />, {
        memoryRouter: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("History");
    });
  });

  describe("only renders header and tablist if route is matching defined tabs", () => {
    afterEach(cleanup);

    it("does render content if the route matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicDetails topicName={testTopic} />, {
        memoryRouter: true,
      });

      const tabList = screen.getByRole("tablist");

      expect(tabList).toBeVisible();
      expect(mockedNavigate).not.toHaveBeenCalled();
    });

    it("redirects user to topic overview if the route does not matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "something",
        },
      ]);

      customRender(<TopicDetails topicName={testTopic} />, {
        memoryRouter: true,
      });

      const tabList = screen.queryByRole("tablist");

      expect(tabList).not.toBeInTheDocument();

      expect(mockedNavigate).toHaveBeenCalled();
    });
  });
});
