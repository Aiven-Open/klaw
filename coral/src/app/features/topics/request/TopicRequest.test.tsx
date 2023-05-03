import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import { mockGetEnvironmentsForTeam } from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import {
  defaultgetTopicAdvancedConfigOptionsResponse,
  mockRequestTopic,
  mockgetTopicAdvancedConfigOptions,
} from "src/domain/topic/topic-api.msw";
import api from "src/services/api";
import { server } from "src/services/api-mocks/server";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { objectHasProperty } from "src/services/type-utils";

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

      it("shows a required select element for 'Environment'", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        expect(select).toBeEnabled();
        expect(select).toBeRequired();
      });

      it("shows an placeholder text for the select", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
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

      it("renders an enabled Submit button", () => {
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        expect(submitButton).toBeEnabled();
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
            name: "Environment *",
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
            name: "Environment *",
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
    describe("informs user about valid input with placeholder per default", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "DEV",
                id: "4",
                params: {
                  applyRegex: false,
                  topicRegex: [".*Dev.*"],
                },
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

      it("shows the default placeholder value", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        await user.selectOptions(select, "DEV");

        const topicNameInput = screen.getByRole("textbox", {
          name: /Topic name/,
        });

        expect(topicNameInput).toHaveAttribute(
          "placeholder",
          "Allowed characters: letter, digit, period, underscore, and hyphen."
        );
      });
    });

    describe("informs user about valid input with placeholder when regex should be applied", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "DEV",
                id: "4",
                params: {
                  applyRegex: true,
                  topicRegex: [".*Dev.*"],
                },
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

      it("shows the allowed pattern in placeholder", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        await user.selectOptions(select, "DEV");

        const topicNameInput = screen.getByRole("textbox", {
          name: /Topic name/,
        });

        expect(topicNameInput).toHaveAttribute(
          "placeholder",
          'Follow name pattern: ".*Dev.*". Allowed characters: letter, digit, period, underscore, and hyphen.'
        );
      });
    });

    describe("informs user about valid input with a prefix is needed", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "DEV",
                id: "4",
                params: {
                  applyRegex: false,
                  topicPrefix: ["prefix_"],
                },
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

      it("shows the allowed pattern in placeholder", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        await user.selectOptions(select, "DEV");

        const topicNameInput = screen.getByRole("textbox", {
          name: /Topic name/,
        });

        expect(topicNameInput).toHaveAttribute(
          "placeholder",
          'Prefix name with: "prefix_". Allowed characters: letter, digit, period, underscore, and hyphen.'
        );
      });
    });

    describe("when topic name does not have enough characters", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicPrefix",
                id: "4",
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

      it("validates that topic name has more then 3 characters", async () => {
        const expectedErrorMsg =
          "Topic name must contain at least 3 characters.";
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });

        await user.selectOptions(select, "EnvWithTopicPrefix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "fo{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "test-foobar{tab}");

        expect(errorMessage).not.toBeVisible();
      });
    });

    describe("when topic name does not match the default pattern", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicPrefix",
                id: "4",
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

      it("validates that topic name matches the default pattern '^[a-zA-Z0-9._-]{3,}$'", async () => {
        const expectedErrorMsg =
          "Topic name can only contain letters, digits, period, underscore, hyphen.";
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });

        await user.selectOptions(select, "EnvWithTopicPrefix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "topic!name{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "test-foobar{tab}");

        expect(errorMessage).not.toBeVisible();
      });
    });

    describe("when environment params have topicPrefix defined", () => {
      beforeEach(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicPrefix",
                id: "4",
                params: { topicPrefix: ["test-"], applyRegex: undefined },
              }),
              createMockEnvironmentDTO({
                name: "EnvWithTopicRegex",
                id: "2",
                params: {
                  applyRegex: true,
                  topicRegex: [".*Dev*."],
                  topicPrefix: ["test-"],
                },
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

      it("validates that topic name starts with environment topic prefix", async () => {
        const expectedErrorMsg = 'Topic name must start with "test-".';
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });

        await user.selectOptions(select, "EnvWithTopicPrefix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "foobar{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "test-foobar{tab}");

        expect(errorMessage).not.toBeVisible();
      });

      it("does not validate the prefix if a regex should be applied", async () => {
        const expectedErrorMsg = 'Topic name must start with "test-".';
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });

        await user.selectOptions(select, "EnvWithTopicRegex");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "foobar{tab}");

        try {
          await screen.findByText(expectedErrorMsg);
          expect(true).toBe(false);
        } catch (error) {
          if (error && objectHasProperty(error, "name")) {
            expect((error as { name: string }).name).toEqual(
              "TestingLibraryElementError"
            );
          } else {
            expect("But it did!").toEqual(
              "Testing library should not have found an element here"
            );
          }
        }
      });
    });

    describe("when environment params have topicSuffix defined", () => {
      beforeAll(() => {
        mockGetEnvironmentsForTeam({
          mswInstance: server,
          response: {
            data: [
              createMockEnvironmentDTO({
                name: "EnvWithTopicSuffix",
                id: "4",
                params: {
                  topicSuffix: ["-test"],
                },
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
          name: "Environment *",
        });

        await user.selectOptions(select, "EnvWithTopicSuffix");

        const topicNameInput = screen.getByLabelText(/Topic name/);
        await user.type(topicNameInput, "foobar{tab}");

        const errorMessage = await screen.findByText(expectedErrorMsg);
        expect(errorMessage).toBeVisible();

        await user.clear(topicNameInput);
        await user.type(topicNameInput, "foobar-test{tab}");

        expect(errorMessage).not.toBeVisible();
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
                params: {
                  maxPartitions: "8",
                  maxRepFactor: "2",
                },
              }),
              createMockEnvironmentDTO({
                name: "PROD",
                id: "3",
                params: {
                  maxPartitions: "16",
                  maxRepFactor: "4",
                },
              }),
              createMockEnvironmentDTO({
                name: "WITH_DEFAULT_PARTITIONS",
                id: "4",
                params: {
                  defaultRepFactor: "4",
                },
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
            name: "Environment *",
          });

          await user.selectOptions(environmentSelect, "PROD");

          const replicationFactorSelect = screen.getByRole("combobox", {
            name: "Replication factor *",
          });
          expect(replicationFactorSelect).toBeEnabled();
        });

        it('should render <input type="number" /> when "maxReplicationFactor" not defined', async () => {
          const environmentSelect = await screen.findByRole("combobox", {
            name: "Environment *",
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
                params: {
                  maxPartitions: "8",
                  maxRepFactor: "2",
                },
              }),
              createMockEnvironmentDTO({
                name: "PROD",
                id: "3",
                params: {
                  maxPartitions: "16",
                  maxRepFactor: "4",
                },
              }),
              createMockEnvironmentDTO({
                name: "WITH_DEFAULT_PARTITIONS",
                id: "4",
                params: {
                  defaultRepFactor: "4",
                },
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

      it('changes replication factor value to environment params "defaultPartitions"', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        const inputReplicationFactor = await screen.findByLabelText(
          "Replication factor*"
        );

        expect(inputReplicationFactor).toHaveDisplayValue("");

        await user.selectOptions(selectEnvironment, "WITH_DEFAULT_PARTITIONS");

        expect(inputReplicationFactor).toHaveDisplayValue("4");
      });

      it('changes replication factor value to environment params "maxPartitions" when exceeded it is and no default', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment *",
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
          name: "Environment *",
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
              params: {
                maxPartitions: "8",
                maxRepFactor: "2",
              },
            }),
            createMockEnvironmentDTO({
              name: "PROD",
              id: "3",
              params: {
                maxPartitions: "16",
                maxRepFactor: "4",
              },
            }),
            createMockEnvironmentDTO({
              name: "WITH_DEFAULT_PARTITIONS",
              id: "4",
              params: {
                defaultPartitions: "4",
              },
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
          name: "Environment *",
        });
        await user.selectOptions(selectEnvironment, "PROD");

        const topicPartitionSelect = screen.getByRole("combobox", {
          name: "Topic partitions *",
        });
        expect(topicPartitionSelect).toBeEnabled();
      });

      it('should render <input type="number" /> when "maxPartitions" is not defined', async () => {
        const selectEnvironment = await screen.findByRole("combobox", {
          name: "Environment *",
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
              params: {
                maxPartitions: "8",
                maxRepFactor: "2",
              },
            }),
            createMockEnvironmentDTO({
              name: "PROD",
              id: "3",
              params: {
                maxPartitions: "16",
                maxRepFactor: "4",
              },
            }),
            createMockEnvironmentDTO({
              name: "WITH_DEFAULT_PARTITIONS",
              id: "4",
              params: {
                defaultPartitions: "4",
              },
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

    it('changes topic partitions value to 4 when environment params have "defaultPartitions"', async () => {
      const selectEnvironment = await screen.findByRole("combobox", {
        name: "Environment *",
      });
      await user.selectOptions(selectEnvironment, "WITH_DEFAULT_PARTITIONS");

      const topicPartitionsInput = screen.getByRole("spinbutton", {
        name: "Topic partitions *",
      });
      expect(topicPartitionsInput).toHaveDisplayValue("4");
    });

    it('changes topic partitions value to environment params "maxPartitions" when current value exceeds', async () => {
      const selectEnvironment = await screen.findByRole("combobox", {
        name: "Environment *",
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
        name: "Environment *",
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
      await screen.findByRole("combobox", {
        name: "Environment *",
      });
      await screen.findByRole("option", { name: "DEV" });
    });

    afterAll(() => {
      cleanup();
    });

    beforeEach(async () => {
      // Fill form with valid data
      await user.selectOptions(
        screen.getByRole("combobox", {
          name: "Environment *",
        }),
        "DEV"
      );
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

    describe("handles an error from the api", () => {
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
          requestOperationType: "CREATE",
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Topic with such name already exists!");
      });
    });

    describe("enables user to create a new topic request", () => {
      beforeEach(async () => {
        mockRequestTopic({
          mswInstance: server,
          response: {
            status: 200,
            data: { success: true, message: "success" },
          },
        });
      });

      it("creates a new topic request when input was valid", async () => {
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
          requestOperationType: "CREATE",
        });
      });

      it("errors and does not create a new topic request when input was invalid", async () => {
        const spyPost = jest.spyOn(api, "post");

        await user.clear(screen.getByLabelText("Topic name*"));

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() =>
          expect(screen.getByText("Topic name can not be empty")).toBeVisible()
        );

        expect(spyPost).not.toHaveBeenCalled();
        expect(
          screen.getByRole("button", { name: "Submit request" })
        ).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        const spyPost = jest.spyOn(api, "post");

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() => {
          const btn = screen.getByRole("button", { name: "Submit request" });
          expect(btn).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Topic request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        const spyPost = jest.spyOn(api, "post");

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() => {
          const btn = screen.getByRole("button", { name: "Submit request" });
          expect(btn).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedUsedNavigate).toHaveBeenCalledWith(
          "/requests/topics?status=CREATED"
        );
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
      await screen.findByRole("combobox", {
        name: "Environment *",
      });
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
      expect(dialog).toHaveTextContent("Cancel topic request?");
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
