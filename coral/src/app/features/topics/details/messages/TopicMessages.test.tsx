import { cleanup, screen, waitFor } from "@testing-library/react";
import { getTopicMessages } from "src/domain/topic/topic-api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicMessages } from "src/app/features/topics/details/messages/TopicMessages";
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
  it("informs user to specify offset and fetch topic messages", async () => {
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
    screen.getByText(
      "To view messages in this topic, select the number of messages you'd like to view and select Fetch messages."
    );
  });
  it("requests and displays all messages when Update results is pressed", async () => {
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
    await userEvent.click(
      screen.getByRole("button", {
        name: "Fetch and display the latest 5 messages from topic test",
      })
    );
    await waitFor(() => expect(mockGetTopicMessages).toHaveBeenCalledTimes(1));
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
    await userEvent.click(
      screen.getByRole("button", {
        name: "Fetch and display the latest 5 messages from topic test",
      })
    );
    await waitFor(() => {
      screen.getByText("This Topic contains no messages.");
    });
  });

  describe("user can filter messages by offset", () => {
    beforeEach(() => {
      mockGetTopicMessages.mockResolvedValue(mockGetTopicMessagesResponse);
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", async () => {
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
      await userEvent.click(
        screen.getByRole("button", {
          name: "Fetch and display the latest 25 messages from topic test",
        })
      );
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(1, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "25",
        });
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
      await userEvent.click(screen.getByRole("radio", { name: "50" }));
      await userEvent.click(
        screen.getByRole("button", {
          name: "Fetch and display the latest 50 messages from topic test",
        })
      );
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(1, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "50",
        });
      });
    });
  });
});
