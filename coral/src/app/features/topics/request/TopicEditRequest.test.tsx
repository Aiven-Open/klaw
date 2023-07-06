import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicEditRequest from "src/app/features/topics/request/TopicEditRequest";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { editTopic, getTopicDetailsPerEnv } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";

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
    topicName: "test-topic-name",
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

describe("<TopicEditRequest />", () => {
  const originalConsoleError = console.error;

  beforeAll(() => {
    console.error = jest.fn();
    mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue(mockEnvironments);
    mockGetTopicDetailsPerEnv.mockResolvedValue(mockTopicDetails);

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
        customRoutePath: "/topic/test-topic-name/request-update?env=1",
      }
    );
  });

  afterEach(() => {
    mockedUsedNavigate.mockClear();
  });

  afterAll(() => {
    console.error = originalConsoleError;
    cleanup();
    jest.clearAllMocks();
  });

  describe("Renders all fields with correct default values and disabled states", () => {
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
      expect(input).toHaveDisplayValue("test-topic-name");
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
      expect(mockedAdvancedConfig).toHaveDisplayValue(
        JSON.stringify({ "cleanup.policy": "compact" })
      );
    });

    it("shows a readOnly text input element for 'Description' with correct default value", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Description *",
      });
      const description = mockTopicDetails.topicContents?.description as string;

      expect(input).toBeRequired();
      expect(input).toHaveDisplayValue(description);
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
    it("creates a new topic update request when input was valid", async () => {
      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditTopic).toHaveBeenCalledTimes(1);
      expect(mockEditTopic).toHaveBeenCalledWith({
        advancedConfiguration: '{"cleanup.policy":"compact"}',
        description: "Topic description",
        environment: {
          id: "1",
          name: "DEV",
          params: {
            applyRegex: undefined,
            defaultPartitions: 2,
            defaultRepFactor: 1,
            maxPartitions: 2,
            maxRepFactor: 1,
            topicPrefix: undefined,
            topicSuffix: undefined,
          },
          type: "kafka",
        },
        remarks: "",
        replicationfactor: "1",
        topicname: "test-topic-name",
        topicpartitions: "2",
      });
    });

    it("shows a notification that request was successful and redirects user", async () => {
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

    it("redirects user to the previous page if they click 'Cancel' on form without changes", async () => {
      await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });

    it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
      const input = await screen.findByRole("textbox", {
        name: "Message for approval",
      });

      await userEvent.type(input, "hello");
      await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
      expect(
        within(dialog).getByText("Cancel topic update request?")
      ).toBeVisible();

      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });

    it("brings the user back to the form when they do not cancel", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Message for approval",
      });

      await userEvent.type(input, "hello");
      await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

      const dialog = screen.getByRole("dialog");
      const continueButton = within(dialog).getByRole("button", {
        name: "Continue with request",
      });

      expect(dialog).toBeVisible();
      expect(
        within(dialog).getByText("Cancel topic update request?")
      ).toBeVisible();

      await userEvent.click(continueButton);

      expect(mockedUsedNavigate).not.toHaveBeenCalled();

      expect(dialog).not.toBeInTheDocument();
    });

    it("redirects user to previous page if they cancel the request", async () => {
      const input = await screen.findByRole("textbox", {
        name: "Message for approval",
      });

      await userEvent.type(input, "hello");
      await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

      const dialog = screen.getByRole("dialog");
      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel request",
      });

      expect(dialog).toBeVisible();
      expect(
        within(dialog).getByText("Cancel topic update request?")
      ).toBeVisible();

      await userEvent.click(cancelButton);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });
  });
});
