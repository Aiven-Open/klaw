import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { Context as AquariumContext } from "@aivenio/aquarium";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetEnvironmentsForTeam } from "src/domain/environment/environment-api.msw";
import { server } from "src/services/api-mocks/server";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import {
  defaultGetTopicAdvanvedConfigOptionsResponse,
  mockGetTopicAdvanvedConfigOptions,
} from "src/domain/topic/topic-api.msw";

describe("<TopicRequest />", () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe("Environment select", () => {
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
      mockGetTopicAdvanvedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultGetTopicAdvanvedConfigOptionsResponse,
        },
      });
      customRender(<TopicRequest />, { queryClient: true });
    });
    afterAll(() => {
      cleanup();
    });

    it("input with label 'Environment' exists", async () => {
      await screen.findByLabelText("Environment");
    });

    it("defaults to empty value", () => {
      const prodSelectOption: HTMLOptionElement = screen.getByRole("option", {
        name: "-- Select Environment --",
      });

      expect(prodSelectOption.selected).toBe(true);
    });

    describe("when field is clicked", () => {
      beforeEach(async () => {
        await user.click(screen.getByLabelText("Environment"));
      });

      it("shows environment names as options", () => {
        const options = screen.getAllByRole("option");
        expect(options.length).toBe(4);
        expect(options.map((o) => o.textContent)).toEqual([
          "-- Select Environment --",
          "DEV",
          "TST",
          "PROD",
        ]);
      });

      describe("when 'PROD' option is clicked", () => {
        beforeEach(async () => {
          await user.selectOptions(
            screen.getByLabelText("Environment"),
            "PROD"
          );
        });

        it("'PROD' value is selected", async () => {
          const prodSelectOption: HTMLOptionElement = screen.getByRole(
            "option",
            { name: "PROD" }
          );
          expect(prodSelectOption.selected).toBe(true);
        });

        it("disabled the placeholder value", () => {
          const options = within(
            screen.getByLabelText("Environment")
          ).getAllByRole("option");
          expect(
            within(screen.getByLabelText("Environment")).getByRole("option", {
              name: "-- Select Environment --",
            })
          ).toBeDisabled();
          expect(options.length).toBe(4);
          expect(options.map((o) => o.textContent)).toEqual([
            "-- Select Environment --",
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
      beforeAll(async () => {
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
        mockGetTopicAdvanvedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultGetTopicAdvanvedConfigOptionsResponse,
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
        await screen.findByLabelText("Environment");
        const expectedErrorMsg = 'Topic name must start with "test-".';
        await user.selectOptions(
          screen.getByLabelText("Environment"),
          "EnvWithTopicPrefix"
        );

        await user.type(screen.getByLabelText(/Topic name/), "foobar{tab}");
        await screen.findByText(expectedErrorMsg);
        await user.clear(screen.getByLabelText(/Topic name/));
        await user.type(
          screen.getByLabelText(/Topic name/),
          "test-foobar{tab}"
        );
        await waitFor(() => {
          expect(screen.queryByText(expectedErrorMsg)).not.toBeInTheDocument();
        });
      });
    });
    describe("when environment has topicsuffix defined", () => {
      beforeAll(async () => {
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
        mockGetTopicAdvanvedConfigOptions({
          mswInstance: server,
          response: {
            data: defaultGetTopicAdvanvedConfigOptionsResponse,
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
        await screen.findByLabelText("Environment");
        const expectedErrorMsg = 'Topic name must end with "-test".';
        await user.selectOptions(
          screen.getByLabelText("Environment"),
          "EnvWithTopicSuffix"
        );

        await user.type(screen.getByLabelText(/Topic name/), "foobar{tab}");
        await screen.findByText('Topic name must end with "-test".');
        await user.clear(screen.getByLabelText(/Topic name/));
        await user.type(
          screen.getByLabelText(/Topic name/),
          "foobar-test{tab}"
        );
        await waitFor(() => {
          expect(screen.queryByText(expectedErrorMsg)).not.toBeInTheDocument();
        });
      });
    });
  });

  describe("Replication factor", () => {
    beforeAll(async () => {
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
      mockGetTopicAdvanvedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultGetTopicAdvanvedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );
      // Wait environments to be loaded
      await screen.findByLabelText("Environment");
    });
    afterAll(() => {
      cleanup();
    });

    describe("input components", () => {
      it('should render <select /> when "maxReplicationFactor" is defined', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        screen.getByRole("combobox", { name: "Replication factor" });
      });

      it('should render <input type="number" /> when "maxReplicationFactor" not defined', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        screen.getByRole("spinbutton", { name: "Replication factor" });
      });
    });

    describe("when environment is changed", () => {
      it('changes replication factor value to environment "defaultPartitions"', async () => {
        await user.selectOptions(
          screen.getByLabelText("Environment"),
          "WITH_DEFAULT_PARTITIONS"
        );
        await waitFor(() => {
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("4");
        });
      });

      it('changes replication factor value to environment "maxPartitions" when exceeded it is and no default', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await user.clear(screen.getByLabelText("Replication factor"));
        await user.type(screen.getByLabelText("Replication factor"), "100");
        expect(screen.getByLabelText("Replication factor")).toHaveDisplayValue(
          "100"
        );

        await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        await waitFor(() => {
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("2");
        });
      });

      it('keeps topic replication factor value if not default and value does not exceeded "maxPartitions"', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        await user.selectOptions(
          screen.getByLabelText("Replication factor"),
          "4"
        );
        await waitFor(() => {
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("4");
        });

        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await waitFor(() => {
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("4");
        });
      });
    });
  });

  describe("Topic partitions", () => {
    beforeAll(async () => {
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
      mockGetTopicAdvanvedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultGetTopicAdvanvedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );
      // Wait environments to be loaded
      await screen.findByLabelText("Environment");
    });
    afterAll(() => {
      cleanup();
    });

    describe("input components", () => {
      it('should render <select /> when "maxPartitions" is defined', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        screen.getByRole("combobox", { name: "Topic partitions" });
      });

      it('should render <input type="number" /> when "maxPartitions" is not defined', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        screen.getByRole("spinbutton", { name: "Topic partitions" });
      });
    });

    describe("when environment is changed", () => {
      it('changes topic partitions value to 4 when environment has "defaultPartitions"', async () => {
        await user.selectOptions(
          screen.getByLabelText("Environment"),
          "WITH_DEFAULT_PARTITIONS"
        );
        expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
          "4"
        );
      });

      it('changes topic partitions value to environment "maxPartitions" when current value exceeds', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await user.clear(screen.getByLabelText("Topic partitions"));
        await user.type(screen.getByLabelText("Topic partitions"), "100");
        await waitFor(() => {
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "100"
          );
        });

        await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        await waitFor(() => {
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "8"
          );
        });
      });

      it('keeps topic partitions value if not default and value does not exceeded "maxPartitions"', async () => {
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        await user.selectOptions(
          screen.getByLabelText("Topic partitions"),
          "16"
        );
        expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
          "16"
        );

        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await waitFor(() => {
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "16"
          );
        });
      });
    });
  });

  describe("AdvancedConfiguration", () => {
    beforeAll(async () => {
      mockGetEnvironmentsForTeam({
        mswInstance: server,
        response: {
          data: [createMockEnvironmentDTO({ name: "DEV", id: "1" })],
        },
      });

      mockGetTopicAdvanvedConfigOptions({
        mswInstance: server,
        response: {
          data: defaultGetTopicAdvanvedConfigOptionsResponse,
        },
      });

      customRender(
        <AquariumContext>
          <TopicRequest />
        </AquariumContext>,
        { queryClient: true }
      );

      await screen.findByLabelText("Environment");
      await screen.findByRole("option", { name: "DEV" });
    });
    afterAll(() => {
      cleanup();
    });

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

    it("renders a field which accespts JSON values", async () => {
      const mockedAdvancedConfig = screen.getByTestId("advancedConfiguration");
      await user.type(mockedAdvancedConfig, '{{"another":"value"}');
      expect(mockedAdvancedConfig).toHaveDisplayValue(
        JSON.stringify({ another: "value" })
      );
    });
  });
});
