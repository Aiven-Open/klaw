import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { Context as AquariumContext } from "@aivenio/aquarium";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockGetEnvironmentsForTeam } from "src/domain/environment/environment-api.msw";
import { server } from "src/services/api-mocks/server";
import TopicRequest from "src/app/features/topics/request/TopicRequest";

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
          ],
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

    describe("when 'maxReplicationFactor' defined for selected environment", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        await user.selectOptions(
          screen.getByLabelText("Replication factor"),
          "2"
        );
      });

      it("renders a select field with 1 as default value", () => {
        screen.getByRole("combobox", { name: "Replication factor" });
        expect(screen.getByLabelText("Replication factor")).toHaveDisplayValue(
          "2"
        );
      });

      describe("when environment changed to one without 'MaxReplicationFactor'", () => {
        beforeEach(async () => {
          await screen.findByLabelText("Environment");
          await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        });

        it("should change the input type into number while keeping the value", () => {
          screen.getByRole("spinbutton", { name: "Replication factor" });
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("2");
        });
      });
    });

    describe("when 'maxReplicationFactor' not defined for selected environment", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await user.clear(screen.getByLabelText("Replication factor"));
        await user.type(screen.getByLabelText("Replication factor"), "2");
      });
      it("renders a number input with 1 as default value", () => {
        screen.getByRole("spinbutton", { name: "Replication factor" });
        expect(screen.getByLabelText("Replication factor")).toHaveDisplayValue(
          "2"
        );
      });

      describe("when environment changed to one with 'MaxReplicationFactor'", () => {
        beforeEach(async () => {
          await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        });

        it("should change the input type into select while keeping the value", () => {
          screen.getByRole("combobox", { name: "Replication factor" });
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("2");
        });
      });
    });

    describe("when changing from environment higher 'maxReplicationFactor' into one with smaller one", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        await user.selectOptions(
          screen.getByLabelText("Replication factor"),
          "4"
        );
      });
      it("should clip the value into 'maxReplicationFactor' of the new environment", async () => {
        expect(screen.getByLabelText("Replication factor")).toHaveDisplayValue(
          "4"
        );
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        waitFor(() => {
          expect(
            screen.getByLabelText("Replication factor")
          ).toHaveDisplayValue("2");
        });
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
          ],
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

    describe("when 'maxPartitions' defined for selected environment", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        await user.selectOptions(
          screen.getByLabelText("Topic partitions"),
          "2"
        );
      });

      it("renders a select field with correct value", () => {
        screen.getByRole("combobox", { name: "Topic partitions" });
        expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
          "2"
        );
      });

      describe("when environment changed to one without 'maxPartitions'", () => {
        beforeEach(async () => {
          await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        });

        it("should change the input type into number while keeping the value", () => {
          screen.getByRole("spinbutton", { name: "Topic partitions" });
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "2"
          );
        });
      });
    });

    describe("when 'maxPartitions' not defined for selected environment", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        await user.clear(screen.getByLabelText("Topic partitions"));
        await user.type(screen.getByLabelText("Topic partitions"), "2");
      });
      it("renders a number input with correct value", () => {
        screen.getByRole("spinbutton", { name: "Topic partitions" });
        expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
          "2"
        );
      });

      describe("when environment changed to one with 'maxPartitions'", () => {
        beforeEach(async () => {
          await user.selectOptions(screen.getByLabelText("Environment"), "TST");
        });

        it("should change the input type into select while keeping the value", () => {
          screen.getByRole("combobox", { name: "Topic partitions" });
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "2"
          );
        });
      });
    });

    describe("when changing from environment higher 'maxPartitions' into one with smaller one", () => {
      beforeEach(async () => {
        await screen.findByLabelText("Environment");
        await user.selectOptions(screen.getByLabelText("Environment"), "PROD");
        await user.selectOptions(
          screen.getByLabelText("Topic partitions"),
          "16"
        );
      });
      it("should clip the value into 'maxReplicationFactor' of the new environment", async () => {
        expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
          "16"
        );
        await user.selectOptions(screen.getByLabelText("Environment"), "DEV");
        waitFor(() => {
          expect(screen.getByLabelText("Topic partitions")).toHaveDisplayValue(
            "8"
          );
        });
      });
    });
  });
});
