import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { getTopicMessages } from "src/domain/topic/topic-api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicMessages } from "src/app/features/topics/overview/messages/TopicMessages";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Outlet, Route, Routes } from "react-router-dom";

jest.mock("src/domain/topic/topic-api.ts");

const mockGetTopicMessages = getTopicMessages as jest.MockedFunction<
  typeof getTopicMessages
>;

const mockGetTopicMessagesResponse = {
  0: "HELLO",
  1: "WORLD",
};

const mockGetTopicMessagesNoContentResponse = {
  status: "failed",
};

function DummyParent() {
  return <Outlet context={{ topicName: "test" }} />;
}

describe("TopicMessages", () => {
  beforeEach(() => {
    mockIntersectionObserver();
  });
  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });
  it("requests and displays all messages", async () => {
    mockGetTopicMessages.mockResolvedValue(mockGetTopicMessagesResponse);
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<TopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    expect(mockGetTopicMessages).toHaveBeenCalledTimes(1);
    expect(mockGetTopicMessages).toHaveBeenCalledWith({
      topicName: "test",
      consumerGroupId: "notdefined",
      envId: "2",
      offsetId: "5",
    });
    screen.getByText("HELLO");
    screen.getByText("WORLD");
  });
  it("informs user of no content when there is no content in the topic", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<TopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    screen.getByText("No Message matched your criteria.");
  });
});
