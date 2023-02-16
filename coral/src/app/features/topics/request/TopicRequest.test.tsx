import {
  cleanup,
  screen,
  waitFor,
  within,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { Context as AquariumContext } from "@aivenio/aquarium";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetEnvironmentsForTeam } from "src/domain/environment/environment-api.msw";
import { server } from "src/services/api-mocks/server";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import {
  defaultgetTopicAdvancedConfigOptionsResponse,
  mockgetTopicAdvancedConfigOptions,
  mockRequestTopic,
} from "src/domain/topic/topic-api.msw";
import api from "src/services/api";

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

describe("<TopicRequest />", () => {
  const originalConsoleError = console.error;
  let user: ReturnType<typeof userEvent.setup>;

  beforeAll(() => {
    server.listen();
    console.error = jest.fn();
  });

  afterAll(() => {
    server.close();
    console.error = originalConsoleError;
  });

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe("Environment select", () => {
    describe("renders all necessary elements by default", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({ name: "DEV", id: "1" }),
              createMockEnvironmentDTO({ name: "TST", id: "2" }),
              createMockEnvironmentDTO({ name: "PROD", id: "3" }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });
        customRender(<TopicRequest />, { queryClient: true });
      });
      afterAll(() => {
        cleanup();
      });

      it("shows a select element for 'Environment'", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment",
        });
        expect(select).toBeEnabled();
      });

      it("shows an placeholder text for the select", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment",
        });

        expect(select).toHaveDisplayValue("-- Please select --");
      });

      it("shows all environment names as options", () => {
        const options = screen.getAllByRole("option");
        // 3 environments + option for placeholder
        expect(options.length).toBe(4);
        expect(options.map((o) => o.textContent)).toEqual([
          "-- Please select --",
          "DEV",
          "TST",
          "PROD",
        ]);
      });
    });

    describe("when field is clicked", () => {
      beforeEach(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({ name: "DEV", id: "1" }),
              createMockEnvironmentDTO({ name: "TST", id: "2" }),
              createMockEnvironmentDTO({ name: "PROD", id: "3" }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });
        customRender(<TopicRequest />, { queryClient: true });
      });

      afterEach(() => {
        cleanup();
      });

      describe("when 'PROD' option is clicked", () => {
        it("selects 'PROD' value when user chooses the option", async () => {
          const select = await screen.findByRole("combobox", {
            name: "Environment",
          });
          expect(select).toHaveDisplayValue("-- Please select --");

          await user.selectOptions(select, "PROD");

          const prodSelectOption: HTMLOptionElement = screen.getByRole(
            "option",
            { name: "PROD" }
          );
          expect(prodSelectOption.selected).toBe(true);
          expect(select).toHaveDisplayValue("PROD");
        });

        it("disabled the placeholder value", async () => {
          const select = await screen.findByRole("combobox", {
            name: "Environment",
          });

          const options = within(select).getAllByRole("option");
          const placeholderOption = within(select).getByRole("option", {
            name: "-- Please select --",
          });

          expect(placeholderOption).toBeDisabled();
          expect(options.length).toBe(4);
          expect(options.map((o) => o.textContent)).toEqual([
            "-- Please select --",
            "DEV",
            "TST",
            "PROD",
          ]);
        });
      });
    });
  });

  describe("Topic name", () => {
    describe("when environment has topicprefix defined", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicPrefix",
                id: "4",
                topicprefix: "test-",
              }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });

        customRender(
          <AquariumContext>
            <TopicRequest />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterAll(() => {
        cleanup();
      });

      it("validates that topic name starts with environment topic prefix", async () => {
        const expectedErrorMsg = 'Topic name must start with "test-".';
        const select = await screen.findByRole("combobox", {
          name: "Environment",
        });

        await user.selectOptions(select, "EnvWithTopicPrefix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "foobar{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "test-foobar{tab}");

        await waitForElementToBeRemoved(errorMessage);
      });
    });

    describe("when environment has topicsuffix defined", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicSuffix",
                id: "4",
                topicsuffix: "-test",
              }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });

        customRender(
          <AquariumContext>
            <TopicRequest />
          </AquariumContext>,
          { queryClient: true }
        );
      });
      afterAll(() => {
        cleanup();
      });

      it("validates that topic name ends with environment topic suffix", async () => {
        const expectedErrorMsg = 'Topic name must end with "-test".';
        const select = await screen.findByRole("combobox", {
          name: "Environment",
        });

        await user.selectOptions(select, "EnvWithTopicSuffix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "foobar{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "foobar-test{tab}");

        await waitForElementToBeRemoved(errorMessage);
      });
    });
  });

  describe("Replication factor", () => {
    describe("renders all necessary elements on default", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({ name: "DEV", id: "1" }),
              createMockEnvironmentDTO({
                name: "TST",
                id: "2",
                maxPartitions: "8",
                maxReplicationFactor: "2",
              }),
              createMockEnvironmentDTO({
                name: "PROD",
                id: "3",
                maxPartitions: "16",
                maxReplicationFactor: "4",
              }),
              createMockEnvironmentDTO({
                name: "WITH_DEFAULT_PARTITIONS",
                id: "4",
                defaultReplicationFactor: "4",
              }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });

        customRender(
          <AquariumContext>
            <TopicRequest />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterAll(() => {
        cleanup();
      });

      describe("input components", () => {
        it('should render <select /> when "maxReplicationFactor" is defined', async () => {
          const environmentSelect = await screen.findByRole("combobox", {
            name: "Environment",
          });

          await user.selectOptions(environmentSelect, "PROD");

          const replicationFactorSelect = screen.getByRole("combobox", {
            name: "Replication factor *",
          });
          expect(replicationFactorSelect).toBeEnabled();
        });

        it('should render <input type="number" /> when "maxReplicationFactor" not defined', async () => {
          const environmentSelect = await screen.findByRole("combobox", {
            name: "Environment",
          });
          await user.selectOptions(environmentSelect, "DEV");

          const inputReplicationFactor = screen.getByRole("spinbutton", {
            name: "Replication factor *",
          });
          expect(inputReplicationFactor).toBeEnabled();
        });
      });
    });

    describe("when environment is changed", () => {
      beforeEach(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({ name: "DEV", id: "1" }),
              createMockEnvironmentDTO({
                name: "TST",
                id: "2",
                maxPartitions: "8",
                maxReplicationFactor: "2",
              }),
              createMockEnvironmentDTO({
                name: "PROD",
                id: "3",
                maxPartitions: "16",
                maxReplicationFactor: "4",
              }),
              createMockEnvironmentDTO({
                name: "WITH_DEFAULT_PARTITIONS",
                id: "4",
                defaultReplicationFactor: "4",
              }),
            ],
          },
        });
        mockgetTopicAdvancedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultgetTopicAdvancedConfigOptionsResponse,
          },
        });

        customRender(
          <AquariumContext>
            <TopicRequest />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterEach(() => {
        cleanup();
      });

      it('changes replication factor value to environment "defaultPartitions"', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment",
        });
        const inputReplicationFactor = await screen.findByLabelText(
          "Replication factor*"
        );

        expect(inputReplicationFactor).toHaveDisplayValue("");

        await user.selectOptions(selectEnvironment, "WITH_DEFAULT_PARTITIONS");

        expect(inputReplicationFactor).toHaveDisplayValue("4");
      });

      it('changes replication factor value to environment "maxPartitions" when exceeded it is and no default', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment",
        });
        await user.selectOptions(selectEnvironment, "DEV");

        const inputReplicationFactorInput = await screen.findByRole(
          "spinbutton",
          {
            name: "Replication factor *",
          }
        );

        await user.clear(inputReplicationFactorInput);
        await user.type(inputReplicationFactorInput, "100");

        expect(inputReplicationFactorInput).toHaveDisplayValue("100");

        await user.selectOptions(selectEnvironment, "TST");
        expect(inputReplicationFactorInput).not.toBeInTheDocument();

        const inputReplicationFactorSelect = await screen.findByRole(
          "combobox",
          {
            name: "Replication factor *",
          }
        );

        await waitFor(() => {
          expect(inputReplicationFactorSelect).toHaveDisplayValue("2");
        });
      });

      it('keeps topic replication factor value if not default and value does not exceeded "maxPartitions"', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment",
        });
        await user.selectOptions(selectEnvironment, "PROD");

        const replicationFactorSelect = screen.getByRole("combobox", {
          name: "Replication factor *",
        });

        await user.selectOptions(replicationFactorSelect, "4");
        expect(replicationFactorSelect).toHaveDisplayValue("4");

        await user.selectOptions(selectEnvironment, "DEV");
        expect(replicationFactorSelect).not.toBeInTheDocument();

        const replicationFactorInput = screen.getByRole("spinbutton", {
          name: "Replication factor *",
        });
        expect(replicationFactorInput).toHaveDisplayValue("4");
      });
    });
  });

  describe("Topic partitions", () => {
    beforeAll(() => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [
            createMockEnvironmentDTO({ name: "DEV", id: "1" }),
            createMockEnvironmentDTO({
              name: "TST",
              id: "2",
              maxPartitions: "8",
              maxReplicationFactor: "2",
            }),
            createMockEnvironmentDTO({
              name: "PROD",
              id: "3",
              maxPartitions: "16",
              maxReplicationFactor: "4",
            }),
            createMockEnvironmentDTO({
              name: "WITH_DEFAULT_PARTITIONS",
              id: "4",
              defaultPartitions: "4",
            }),
          ],
        },
      });
      mockgetTopicAdvancedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultgetTopicAdvancedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });
    afterAll(cleanup);

    describe("input components", () => {
      it('should render <select /> when "maxPartitions" is defined', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment",
        });
        await user.selectOptions(selectEnvironment, "PROD");

        const topicPartitionSelect = screen.getByRole("combobox", {
          name: "Topic partitions *",
        });
        expect(topicPartitionSelect).toBeEnabled();
      });

      it('should render <input type="number" /> when "maxPartitions" is not defined', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment",
        });
        await user.selectOptions(selectEnvironment, "DEV");

        const topicPartitionInput = screen.getByRole("spinbutton", {
          name: "Topic partitions *",
        });
        expect(topicPartitionInput).toBeEnabled();
      });
    });
  });

  describe("when environment is changed", () => {
    beforeEach(() => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [
            createMockEnvironmentDTO({ name: "DEV", id: "1" }),
            createMockEnvironmentDTO({
              name: "TST",
              id: "2",
              maxPartitions: "8",
              maxReplicationFactor: "2",
            }),
            createMockEnvironmentDTO({
              name: "PROD",
              id: "3",
              maxPartitions: "16",
              maxReplicationFactor: "4",
            }),
            createMockEnvironmentDTO({
              name: "WITH_DEFAULT_PARTITIONS",
              id: "4",
              defaultPartitions: "4",
            }),
          ],
        },
      });
      mockgetTopicAdvancedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultgetTopicAdvancedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });
    afterEach(cleanup);

    it('changes topic partitions value to 4 when environment has "defaultPartitions"', async () => {
      const selectEnvironment = await screen.findByRole("combobox", {
        name: "Environment",
      });
      await user.selectOptions(selectEnvironment, "WITH_DEFAULT_PARTITIONS");

      const topicPartitionsInput = screen.getByRole("spinbutton", {
        name: "Topic partitions *",
      });
      expect(topicPartitionsInput).toHaveDisplayValue("4");
    });

    it('changes topic partitions value to environment "maxPartitions" when current value exceeds', async () => {
      const selectEnvironment = await screen.findByRole("combobox", {
        name: "Environment",
      });
      await user.selectOptions(selectEnvironment, "DEV");

      const topicPartitionsInput = screen.getByRole("spinbutton", {
        name: "Topic partitions *",
      });
      expect(topicPartitionsInput).toHaveDisplayValue("2");

      await user.clear(topicPartitionsInput);
      await user.type(topicPartitionsInput, "100");

      expect(topicPartitionsInput).toHaveDisplayValue("100");

      await user.selectOptions(selectEnvironment, "TST");
      expect(topicPartitionsInput).not.toBeInTheDocument();

      const topicPartitionsSelect = screen.getByRole("combobox", {
        name: "Topic partitions *",
      });

      await waitFor(() => {
        expect(topicPartitionsSelect).toHaveDisplayValue("8");
      });
    });

    it('keeps topic partitions value if not default and value does not exceeded "maxPartitions"', async () => {
      const selectEnvironment = await screen.findByRole("combobox", {
        name: "Environment",
      });
      await user.selectOptions(selectEnvironment, "PROD");

      const topicPartitionsSelect = screen.getByRole("combobox", {
        name: "Topic partitions *",
      });

      await user.selectOptions(topicPartitionsSelect, "16");
      expect(topicPartitionsSelect).toHaveDisplayValue("16");

      await user.selectOptions(selectEnvironment, "DEV");
      expect(topicPartitionsSelect).toHaveDisplayValue("16");
    });
  });

  describe("AdvancedConfiguration", () => {
    beforeAll(() => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [createMockEnvironmentDTO({ name: "DEV", id: "1" })],
        },
      });

      mockgetTopicAdvancedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultgetTopicAdvancedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });
    afterAll(cleanup);

    it("renders a sub heading", () => {
      screen.getByRole("heading", { name: "Advanced Topic Configuration" });
    });

    it("renders a link to official kafka documentation", () => {
      const link = screen.getByRole("link", {
        name: "Apache Kafka Documentation",
      });
      expect(link).toHaveAttribute(
        "href",
        "https://kafka.apache.org/documentation/#topicconfigs"
      );
    });

    it("renders a field which accepts JSON values", async () => {
      const mockedAdvancedConfig = screen.getByTestId("advancedConfiguration");
      await user.type(mockedAdvancedConfig, '{{"another":"value"}');

      expect(mockedAdvancedConfig).toHaveDisplayValue(
        JSON.stringify({ another: "value" })
      );
    });
  });

  describe("form submission", () => {
    beforeAll(async () => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [createMockEnvironmentDTO({ name: "DEV", id: "1" })],
        },
      });

      mockgetTopicAdvancedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultgetTopicAdvancedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );

      // Wait all API calls to resolve, which are required for the render
      await screen.findByLabelText("Environment");
      await screen.findByRole("option", { name: "DEV" });
    });

    afterAll(() => {
      cleanup();
    });

    beforeEach(async () => {
      // Fill form with valid data
      await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
      await user.clear(screen.getByLabelText("Topic name*"));
      await user.clear(screen.getByLabelText("Topic partitions*"));
      await user.clear(screen.getByLabelText("Replication factor*"));
      await user.clear(screen.getByLabelText("Description*"));

      await user.type(
        screen.getByLabelText("Topic name*"),
        "this-is-topic-name{tab}"
      );
      await user.type(screen.getByLabelText("Topic partitions*"), "3{tab}");
      await user.type(screen.getByLabelText("Replication factor*"), "3{tab}");
      await user.type(screen.getByLabelText("Description*"), "test{tab}");
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe("when API returns an error", () => {
      beforeEach(async () => {
        mockRequestTopic({
          mswInstance: server,
          response: {
            data: { message: "Topic with such name already exists!" },
            status: 400,
          },
        });
      });

      it("renders an error message", async () => {
        const spyPost = jest.spyOn(api, "post");

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() => {
          const btn = screen.getByRole("button", { name: "Submit request" });
          expect(btn).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createTopics", {
          environment: "1",
          topicname: "this-is-topic-name",
          replicationfactor: "3",
          topicpartitions: 3,
          advancedTopicConfigEntries: [],
          description: "test",
          remarks: "",
          topictype: "Create",
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Topic with such name already exists!");
      });
    });
    describe("when API request is successful", () => {
      const locationAssignSpy = jest.fn();
      let originalLocation: Location;

      beforeAll(() => {
        originalLocation = window.location;
        Object.defineProperty(global.window, "location", {
          writable: true,
          value: {
            assign: locationAssignSpy,
          },
        });
      });

      afterAll(() => {
        global.window.location = originalLocation;
      });
      beforeEach(async () => {
        mockRequestTopic({
          mswInstance: server,
          response: { data: { status: "200 OK" } },
        });
      });

      it("redirects user to previous page", async () => {
        const spyPost = jest.spyOn(api, "post");

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() => {
          const btn = screen.getByRole("button", { name: "Submit request" });
          expect(btn).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createTopics", {
          environment: "1",
          topicname: "this-is-topic-name",
          replicationfactor: "3",
          topicpartitions: 3,
          advancedTopicConfigEntries: [],
          description: "test",
          remarks: "",
          topictype: "Create",
        });

        await waitFor(() => {
          expect(locationAssignSpy).toHaveBeenCalledTimes(1);
          expect(locationAssignSpy).toHaveBeenCalledWith(
            "/myTopicRequests?reqsType=created&topicCreated=true"
          );
        });
      });
    });
  });

  describe("enables user to cancel the form input", () => {
    const getForm = () => {
      return screen.getByRole("form", {
        name: `Request a new topic`,
      });
    };

    beforeEach(async () => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [createMockEnvironmentDTO({ name: "DEV", id: "1" })],
        },
      });

      mockgetTopicAdvancedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultgetTopicAdvancedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );

      // Wait all API calls to resolve, which are required for the render
      await screen.findByLabelText("Environment");
      await screen.findByRole("option", { name: "DEV" });
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
      const form = getForm();

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });

    it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
      const form = getForm();

      const nameInput = screen.getByRole("textbox", {
        name: "Topic name *",
      });
      await userEvent.type(nameInput, "Some name");

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);
      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
      expect(dialog).toHaveTextContent("Cancel topic request");
      expect(dialog).toHaveTextContent(
        "Do you want to cancel this request? The data added will be lost."
      );

      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });

    it("brings the user back to the form when they do not cancel", async () => {
      const form = getForm();

      const nameInput = screen.getByRole("textbox", {
        name: "Topic name *",
      });
      await userEvent.type(nameInput, "Some name");

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);
      const dialog = screen.getByRole("dialog");

      const returnButton = screen.getByRole("button", {
        name: "Continue with request",
      });

      await userEvent.click(returnButton);

      expect(mockedUsedNavigate).not.toHaveBeenCalled();

      expect(dialog).not.toBeInTheDocument();
    });

    it("redirects user to previous page if they cancel the request", async () => {
      const form = getForm();

      const nameInput = screen.getByRole("textbox", {
        name: "Topic name *",
      });
      await userEvent.type(nameInput, "Some name");

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);

      const returnButton = screen.getByRole("button", {
        name: "Cancel request",
      });

      await userEvent.click(returnButton);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });
  });
});
