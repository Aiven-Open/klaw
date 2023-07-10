import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicEditRequest from "src/app/features/topics/request/TopicEditRequest";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { editTopic, getTopicDetailsPerEnv } from "src/domain/topic";
import { getTopicAdvancedConfigOptions } from "src/domain/topic/topic-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const TOPIC_NAME = "test-topic-name";
const ENV_ID = "1";
const WRONG_TOPIC_NAME = "does-not-exist";
const WRONG_ENV_ID = "9999";

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/topic/topic-api.ts");
const mockEditTopic = editTopic as jest.MockedFunction<typeof editTopic>;
const mockGetTopicDetailsPerEnv = getTopicDetailsPerEnv as jest.MockedFunction<
  typeof getTopicDetailsPerEnv
>;
const mockgGetTopicAdvancedConfigOptions =
  getTopicAdvancedConfigOptions as jest.MockedFunction<
    typeof getTopicAdvancedConfigOptions
  >;

jest.mock("src/domain/environment/environment-api.ts");
const mockGetAllEnvironmentsForTopicAndAcl =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockEnvironments = transformEnvironmentApiResponse(
  mockedEnvironmentResponse
).map((env) => ({
  ...env,
  params: {
    ...env.params,
    defaultPartitions: 2,
    defaultRepFactor: 1,
    maxPartitions: 2,
    maxRepFactor: 1,
  },
}));

const mockTopicDetails = {
  topicExists: true,
  topicId: "1200",
  topicContents: {
    topicName: TOPIC_NAME,
    noOfPartitions: 2,
    description: "Topic description",
    noOfReplicas: "1",
    teamId: 0,
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    advancedTopicConfiguration: {
      "cleanup.policy": "compact",
    },
  },
};

const mockTransformedGetTopicAdvancedConfigOptions = [
  {
    key: "CLEANUP_POLICY",
    name: "cleanup.policy",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_cleanup.policy",
      text: "Specify the cleanup policy for log segments in a topic.",
    },
  },
  {
    key: "COMPRESSION_TYPE",
    name: "compression.type",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_compression.type",
      text: "Specify the type of compression used for log segments in a topic.",
    },
  },
  {
    key: "DELETE_RETENTION_MS",
    name: "delete.retention.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_delete.retention.ms",
      text: "Specify retention time in milliseconds for the delete tombstone markers for log compacted topics.",
    },
  },
  {
    key: "FILE_DELETE_DELAY_MS",
    name: "file.delete.delay.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_file.delete.delay.ms",
      text: "Specify the wait time in milliseconds before deleting a file from the filesystem.",
    },
  },
  {
    key: "FLUSH_MESSAGES",
    name: "flush.messages",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_flush.messages",
      text: "Specify the number of messages that must be written to a topic before a flush is forced.",
    },
  },
  {
    key: "FLUSH_MS",
    name: "flush.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_flush.ms",
      text: "Specify the wait time in milliseconds before forcing a flush of data.",
    },
  },
  {
    key: "FOLLOWER_REPLICATION_THROTTLED_REPLICAS",
    name: "follower.replication.throttled.replicas",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_follower.replication.throttled.replicas",
      text: "Specify the list of replicas that are currently throttled for replication for this topic.",
    },
  },
  {
    key: "INDEX_INTERVAL_BYTES",
    name: "index.interval.bytes",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_index.interval.bytes",
      text: "Specify the interval at which log file offsets will be indexed.",
    },
  },
  {
    key: "LEADER_REPLICATION_THROTTLED_REPLICAS",
    name: "leader.replication.throttled.replicas",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_leader.replication.throttled.replicas",
      text: "Specify replicas for which log replication should be throttled on the leader side.",
    },
  },
  {
    key: "MAX_COMPACTION_LAG_MS",
    name: "max.compaction.lag.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_max.compaction.lag.ms",
      text: "Specify maximum time a message will remain ineligible for compaction in the log.",
    },
  },
  {
    key: "MAX_MESSAGE_BYTES",
    name: "max.message.bytes",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_max.message.bytes",
      text: "Specify the maximum size in bytes for a batch.",
    },
  },
  {
    key: "MESSAGE_DOWNCONVERSION_ENABLE",
    name: "message.downconversion.enable",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_message.downconversion.enable",
      text: "Enable or disable automatic down conversion of messages.",
    },
  },
  {
    key: "MESSAGE_FORMAT_VERSION",
    name: "message.format.version",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_message.format.version",
      text: "Specify the message format version to be used by the broker to append messages to logs.",
    },
  },
  {
    key: "MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS",
    name: "message.timestamp.difference.max.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.difference.max.ms",
      text: "Specify the maximum time difference in milliseconds allowed between the timestamp of a message and time received.",
    },
  },
  {
    key: "MESSAGE_TIMESTAMP_TYPE",
    name: "message.timestamp.type",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.type",
      text: "Specify if `CreateTime` or `LogAppendTime` should be used as the timestamp of the message.",
    },
  },
  {
    key: "MIN_CLEANABLE_DIRTY_RATIO",
    name: "min.cleanable.dirty.ratio",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_min.cleanable.dirty.ratio",
      text: "Specify the ratio of log to retention size to initiate log compaction.",
    },
  },
  {
    key: "MIN_COMPACTION_LAG_MS",
    name: "min.compaction.lag.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_min.compaction.lag.ms",
      text: "Specify the minimum time a message will remain uncompacted in the log.",
    },
  },
  {
    key: "MIN_INSYNC_REPLICAS",
    name: "min.insync.replicas",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_min.insync.replicas",
      text: "Specify the minimum number of replicas required for a write to be considered successful.",
    },
  },
  {
    key: "PREALLOCATE",
    name: "preallocate",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_preallocate",
      text: "Enable or disable preallocation of file disk for a new log segment.",
    },
  },
  {
    key: "RETENTION_BYTES",
    name: "retention.bytes",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_retention.bytes",
      text: "Specify the maximum size a partition before log segment are discarded to free up space.",
    },
  },
  {
    key: "RETENTION_MS",
    name: "retention.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_retention.ms",
      text: "Specify the retention period in milliseconds for logs before discarding it to free up space.",
    },
  },
  {
    key: "SEGMENT_BYTES",
    name: "segment.bytes",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_segment.bytes",
      text: "Specify the maximum size of a log segment file in bytes.",
    },
  },
  {
    key: "SEGMENT_INDEX_BYTES",
    name: "segment.index.bytes",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_segment.index.bytes",
      text: "Specify the maximum size of the index in bytes that maps offsets to file positions.",
    },
  },
  {
    key: "SEGMENT_JITTER_MS",
    name: "segment.jitter.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_segment.jitter.ms",
      text: "Specify the maximum jitter time in milliseconds.",
    },
  },
  {
    key: "SEGMENT_MS",
    name: "segment.ms",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_segment.ms",
      text: "Specify the maximum time in milliseconds before a log segment is rolled.",
    },
  },
  {
    key: "UNCLEAN_LEADER_ELECTION_ENABLE",
    name: "unclean.leader.election.enable",
    documentation: {
      link: "https://kafka.apache.org/documentation/#topicconfigs_unclean.leader.election.enable",
      text: "Enable or disable unclean leader election.",
    },
  },
];

describe("<TopicEditRequest />", () => {
  describe("Renders all fields with correct default values and disabled states", () => {
    beforeEach(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
      mockGetTopicDetailsPerEnv.mockResolvedValue(mockTopicDetails);
      mockgGetTopicAdvancedConfigOptions.mockResolvedValue(
        mockTransformedGetTopicAdvancedConfigOptions
      );

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/request-update"
            element={
              <AquariumContext>
                <TopicEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/topic/${TOPIC_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a disabled select element for 'Environment' with correct default value", async () => {
      const select = await screen.findByRole("combobox", {
        name: "Environment *",
      });
      expect(select).toBeDisabled();
      expect(select).toBeRequired();
      expect(select).toHaveDisplayValue("DEV");
    });

    it("shows a readOnly text input element for 'Topic name' with correct default value", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Topic name *",
      });
      expect(input).toHaveAttribute("readonly");
      expect(input).toBeRequired();
      expect(input).toHaveDisplayValue(TOPIC_NAME);
    });

    it("shows a select element for 'Topic partitions' with correct default value", async () => {
      const input = await screen.findByRole("combobox", {
        name: "Topic partitions *",
      });
      expect(input).toBeEnabled();
      expect(input).toBeRequired();
      expect(input).toHaveDisplayValue("2");
    });

    it("shows a select element for 'Replication factor' with correct default value", async () => {
      const input = await screen.findByRole("combobox", {
        name: "Replication factor *",
      });
      expect(input).toBeEnabled();
      expect(input).toBeRequired();
      expect(input).toHaveDisplayValue("1");
    });

    it("shows a JSON input field for 'Advanced configuration' with correct default value", async () => {
      const mockedAdvancedConfig = screen.getByTestId("advancedConfiguration");

      expect(mockedAdvancedConfig).toBeEnabled();
      await waitFor(() =>
        expect(mockedAdvancedConfig).toHaveDisplayValue(
          JSON.stringify({ "cleanup.policy": "compact" })
        )
      );
    });

    it("shows a readOnly text input element for 'Description' with correct default value", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Description *",
      });
      const description = mockTopicDetails.topicContents?.description as string;

      expect(input).toBeRequired();
      await waitFor(() => expect(input).toHaveDisplayValue(description));
    });

    it("shows an enabled text input element for 'Message for approval'", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Message for approval",
      });

      expect(input).toBeEnabled();
      expect(input).toHaveDisplayValue("");
    });
  });

  describe("enables user to create a new topic update request", () => {
    beforeEach(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
      mockGetTopicDetailsPerEnv.mockResolvedValue(mockTopicDetails);
      mockgGetTopicAdvancedConfigOptions.mockResolvedValue(
        mockTransformedGetTopicAdvancedConfigOptions
      );

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/request-update"
            element={
              <AquariumContext>
                <TopicEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/topic/${TOPIC_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a notification and does not submit when user has not changed topic data", async () => {
      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditTopic).not.toHaveBeenCalled();

      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "No changes were made to the topic.",
        position: "bottom-left",
        variant: "default",
      });
    });

    it("shows a notification and does not submit when user has changed data, but reverted to initial values", async () => {
      const description = screen.getByRole("textbox", {
        name: "Description *",
      });

      await waitFor(() =>
        expect(description).toHaveDisplayValue(
          mockTopicDetails.topicContents.description
        )
      );

      await userEvent.clear(description);

      await userEvent.type(
        description,
        mockTopicDetails.topicContents.description
      );

      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditTopic).not.toHaveBeenCalled();

      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "No changes were made to the topic.",
        position: "bottom-left",
        variant: "default",
      });
    });

    it("creates a new topic update request when input was valid", async () => {
      const message = screen.getByRole("textbox", {
        name: "Message for approval",
      });

      await userEvent.type(message, "please approve");

      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditTopic).toHaveBeenCalledTimes(1);
      expect(mockEditTopic).toHaveBeenCalledWith({
        advancedConfiguration: JSON.stringify(
          mockTopicDetails.topicContents.advancedTopicConfiguration
        ),
        description: mockTopicDetails.topicContents.description,
        environment: mockEnvironments[0],
        remarks: "please approve",
        replicationfactor: mockTopicDetails.topicContents.noOfReplicas,
        topicname: TOPIC_NAME,
        topicpartitions: String(mockTopicDetails.topicContents.noOfPartitions),
      });
    });

    it("shows a notification that request was successful and redirects user", async () => {
      const message = screen.getByRole("textbox", {
        name: "Message for approval",
      });

      await userEvent.type(message, "please approve");

      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditTopic).toHaveBeenCalled();
      await waitFor(() => {
        expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
      });

      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "Topic update request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    });
  });

  describe("shows an alert message when new topic update request was not successful", () => {
    beforeEach(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
      mockGetTopicDetailsPerEnv.mockResolvedValue(mockTopicDetails);
      mockgGetTopicAdvancedConfigOptions.mockResolvedValue(
        mockTransformedGetTopicAdvancedConfigOptions
      );

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/request-update"
            element={
              <AquariumContext>
                <TopicEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/topic/${TOPIC_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("render an alert when server rejects update request", async () => {
      mockEditTopic.mockRejectedValue({
        success: false,
        message: "Failure. A topic request already exists.",
      });

      await waitFor(() => expect(screen.getByRole("alert")).toBeVisible());
    });
  });

  describe("correctly redirects and warn on navigation when topic does not exist", () => {
    beforeEach(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
      mockGetTopicDetailsPerEnv.mockResolvedValue({ topicExists: false });
      mockgGetTopicAdvancedConfigOptions.mockResolvedValue(
        mockTransformedGetTopicAdvancedConfigOptions
      );

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/request-update"
            element={
              <AquariumContext>
                <TopicEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/topic/${WRONG_TOPIC_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("redirects user to browse topics page", async () => {
      await waitFor(() => {
        expect(mockedUsedNavigate).toHaveBeenCalledWith("/topics", {
          replace: true,
        });
      });
    });

    it("shows a notification that topic name does not exist", async () => {
      await waitFor(() => {
        expect(mockedUseToast).toHaveBeenCalledWith({
          message: `No topic was found with name ${WRONG_TOPIC_NAME}`,
          position: "bottom-left",
          variant: "danger",
        });
      });
    });
  });

  describe("correctly redirects and warn on navigation  when environment does not exist", () => {
    beforeEach(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
      mockGetTopicDetailsPerEnv.mockResolvedValue({ topicExists: false });
      mockgGetTopicAdvancedConfigOptions.mockResolvedValue(
        mockTransformedGetTopicAdvancedConfigOptions
      );

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/request-update"
            element={
              <AquariumContext>
                <TopicEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/topic/${TOPIC_NAME}/request-update?env=${WRONG_ENV_ID}`,
        }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("redirects user to topic overview page", async () => {
      await waitFor(() => {
        expect(mockedUsedNavigate).toHaveBeenCalledWith(
          `/topic/${TOPIC_NAME}`,
          {
            replace: true,
          }
        );
      });
    });

    it("shows a notification that topic name does not exist", async () => {
      await waitFor(() => {
        expect(mockedUseToast).toHaveBeenCalledWith({
          message: `No environment was found with ID ${WRONG_ENV_ID}`,
          position: "bottom-left",
          variant: "danger",
        });
      });
    });
  });
});
