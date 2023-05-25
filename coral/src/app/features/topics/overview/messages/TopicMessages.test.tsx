import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { getTopicMessages } from "src/domain/topic/topic-api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicMessages } from "src/app/features/topics/overview/messages/TopicMessages";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Outlet, Route, Routes } from "react-router-dom";
import userEvent from "@testing-library/user-event";

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
    screen.getByText("This Topic contains no Messages.");
  });

  describe("user can filter messages by offset", () => {
    beforeEach(() => {
      mockGetTopicMessages.mockResolvedValue(mockGetTopicMessagesResponse);
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<TopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: "/?offset=25",
        }
      );
      expect(getTopicMessages).toHaveBeenNthCalledWith(1, {
        topicName: "test",
        consumerGroupId: "notdefined",
        envId: "2",
        offsetId: "25",
      });
    });
    it("applies offset filter by selecting a fixed offset value", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<TopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
      userEvent.click(screen.getByRole("radio", { name: "50" }));
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(2, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "50",
        });
      });
    });
  });

  describe("user can consume latest messages based on selected offset", () => {
    beforeEach(() => {
      mockGetTopicMessages.mockResolvedValue(mockGetTopicMessagesResponse);
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("requests messages from the selected offset", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<TopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
      await userEvent.click(
        screen.getByRole("button", {
          name: "Consume and display the latest 5 messages from topic test",
        })
      );
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(2, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "5",
        });
      });
    });
  });
});
