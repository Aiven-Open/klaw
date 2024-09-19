import { cleanup, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Outlet, Route, Routes } from "react-router-dom";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import TopicMessages from "src/app/features/topics/details/messages/TopicMessages";
import { getTopicMessages } from "src/domain/topic/topic-api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

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

const selectModeOptions = ["Default", "Custom", "Range"];
const WrappedTopicMessages = withFiltersContext({
  defaultValues: { paginated: false },
  element: <TopicMessages />,
});

function DummyParent() {
  return <Outlet context={{ topicName: "test", environmentId: "2" }} />;
}

describe("TopicMessages", () => {
  beforeEach(() => {
    mockIntersectionObserver();
  });
  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });
  it("allows to select between Default, Custom and Range modes", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<WrappedTopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );

    const select = screen.getByRole("combobox", {
      name: "Select mode Choose mode to fetch messages",
    });

    expect(select).toBeEnabled();

    selectModeOptions.forEach((selectMode) => {
      const option = screen.getByRole("option", {
        name: selectMode,
      });
      expect(option).toBeEnabled();
    });
  });

  it("shows selection as Default mode according to URL search params", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<WrappedTopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: "/?defaultOffset=5",
      }
    );

    const select = screen.getByRole("combobox");

    expect(select).toHaveValue("default");
  });

  it("shows selection as Custom mode according to URL search params", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<WrappedTopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: "/?defaultOffset=custom&customOffset=20partitionId=1",
      }
    );

    expect(screen.getByRole("combobox")).toHaveValue("custom");
    expect(
      screen.getByRole("spinbutton", {
        name: "Partition ID * Enter partition ID to retrieve last messages",
      })
    ).toBeVisible();
    expect(
      screen.getByRole("spinbutton", {
        name: "Number of messages * Set the number of recent messages to display from this partition",
      })
    ).toBeVisible();
  });

  it("informs user to specify offset and fetch topic messages", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/?" element={<WrappedTopicMessages />} />
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

  it("shows selection as Range mode according to URL search params", async () => {
    mockGetTopicMessages.mockResolvedValue(
      mockGetTopicMessagesNoContentResponse
    );
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<WrappedTopicMessages />} />
        </Route>
      </Routes>,
      {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: "/?defaultOffset=range",
      }
    );

    expect(screen.getByRole("combobox")).toHaveValue("range");
    expect(
      screen.getByRole("spinbutton", {
        name: "Partition ID * Enter partition ID to retrieve last messages",
      })
    ).toBeVisible();
    expect(
      screen.getByRole("spinbutton", {
        name: "Start Offset * Set the start offset",
      })
    ).toBeVisible();
    expect(
      screen.getByRole("spinbutton", {
        name: "End Offset * Set the end offset",
      })
    ).toBeVisible();
  });

  it("requests and displays all messages when Update results is pressed", async () => {
    mockGetTopicMessages.mockResolvedValue(mockGetTopicMessagesResponse);
    customRender(
      <Routes>
        <Route path="/" element={<DummyParent />}>
          <Route path="/" element={<WrappedTopicMessages />} />
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
      selectedOffsetRangeStart: 0,
      selectedOffsetRangeEnd: 0,
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
          <Route path="/" element={<WrappedTopicMessages />} />
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
            <Route path="/" element={<WrappedTopicMessages />} />
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
          selectedOffsetRangeStart: 0,
          selectedOffsetRangeEnd: 0,
        });
      });
    });
    it("populates the filter from the url search parameters (partitionId and customOffset)", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<WrappedTopicMessages />} />
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
          selectedOffsetRangeStart: 0,
          selectedOffsetRangeEnd: 0,
        });
      });
    });
    it("applies offset filter by selecting a fixed offset value", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<WrappedTopicMessages />} />
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
          selectedOffsetRangeStart: 0,
          selectedOffsetRangeEnd: 0,
        });
      });
    });
    it("populates the filter from the url search parameters (partitionId, rangeOffsetStart and rangeOffsetEnd)", async () => {
      customRender(
        <Routes>
          <Route path="/" element={<DummyParent />}>
            <Route path="/" element={<WrappedTopicMessages />} />
          </Route>
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath:
            "/?defaultOffset=range&partitionId=1&rangeOffsetStart=5&rangeOffsetEnd=10",
        }
      );

      await userEvent.click(
        screen.getByRole("button", {
          name: "Fetch and display the messages from offset 5 to offset 10 from partiton 1 of topic test",
        })
      );
      await waitFor(() => {
        expect(getTopicMessages).toHaveBeenNthCalledWith(1, {
          topicName: "test",
          consumerGroupId: "notdefined",
          envId: "2",
          offsetId: "range",
          selectedNumberOfOffsets: 0,
          selectedPartitionId: 1,
          selectedOffsetRangeStart: 5,
          selectedOffsetRangeEnd: 10,
        });
      });
    });
  });
});
