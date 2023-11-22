import { cleanup, screen, waitFor } from "@testing-library/react";
import { getTopicMessages } from "src/domain/topic/topic-api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicMessages } from "src/app/features/topics/details/messages/TopicMessages";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Outlet, Route, Routes } from "react-router-dom";
import { userEvent } from "@testing-library/user-event";

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
  it("allows to switch between Default and Custom modes", async () => {
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

    const switchInput = screen.getByRole("checkbox");

    const switchGroupDefault = screen.getByRole("group", {
      name: "Fetching mode Get the latest messages",
    });

    expect(switchGroupDefault).toBeVisible();
    expect(switchInput).not.toBeChecked();

    await userEvent.click(switchInput);

    const switchGroupCustom = screen.getByRole("group", {
      name: "Fetching mode Get a specific offset",
    });

    expect(switchGroupCustom).toBeVisible();
    expect(switchInput).toBeChecked();
  });

  it("shows switch as Default mode according to URL search params", async () => {
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
        customRoutePath: "/?defaultOffset=5",
      }
    );

    const switchInput = screen.getByRole("checkbox");

    const switchGroupDefault = screen.getByRole("group", {
      name: "Fetching mode Get the latest messages",
    });

    expect(switchGroupDefault).toBeVisible();
    expect(switchInput).not.toBeChecked();
  });

  it("shows switch as Custom mode according to URL search params", async () => {
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
        customRoutePath: "/?defaultOffset=custom&customOffset=20partitionId=1",
      }
    );

    const switchInput = screen.getByRole("checkbox");

    const switchGroupCustom = screen.getByRole("group", {
      name: "Fetching mode Get a specific offset",
    });

    expect(switchGroupCustom).toBeVisible();
    expect(switchInput).toBeChecked();
  });

  it("informs user to specify offset and fetch topic messages", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/?" element={<TopicMessages />} />
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
      selectedNumberOfOffsets: 0,
      selectedPartitionId: 0,
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
      screen.getByText("This topic contains no messages.");
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

    it("populates the filter from the url search parameters (defaultOffset)", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<TopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: "/?defaultOffset=25",
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
          selectedNumberOfOffsets: 0,
          selectedPartitionId: 0,
        });
      });
    });
    it("populates the filter from the url search parameters (partitionId and customOffset)", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<TopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath:
            "/?defaultOffset=custom&partitionId=1&customOffset=20",
        }
      );
      await userEvent.click(
        screen.getByRole("button", {
          name: "Fetch and display the latest 20 messages from partiton 1 of topic test",
        })
      );
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(1, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "custom",
          selectedNumberOfOffsets: 20,
          selectedPartitionId: 1,
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
          selectedNumberOfOffsets: 0,
          selectedPartitionId: 0,
        });
      });
    });
  });
});
