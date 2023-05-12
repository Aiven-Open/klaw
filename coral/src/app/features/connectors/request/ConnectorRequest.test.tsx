import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { createConnectorRequest } from "src/domain/connector";
import { getAllEnvironmentsForConnector } from "src/domain/environment";

vi.mock("src/domain/environment/environment-api.ts");
vi.mock("src/domain/connector/connector-api.ts");

const mockGetConnectorEnvironmentRequest = vi.mocked(
  getAllEnvironmentsForConnector
);

const mockCreateConnectorRequest = vi.mocked(createConnectorRequest);

const mockedUsedNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
  ...vi.importActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

describe("<ConnectorRequest />", () => {
  const originalConsoleError = console.error;
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    console.error = vi.fn();
    user = userEvent.setup();
  });

  afterEach(() => {
    console.error = originalConsoleError;
  });

  describe("Environment select", () => {
    describe("renders all necessary elements by default", () => {
      beforeEach(() => {
        mockGetConnectorEnvironmentRequest.mockResolvedValue([
          createEnvironment({ id: "1", name: "DEV" }),
          createEnvironment({ id: "2", name: "TST" }),
          createEnvironment({ id: "3", name: "PROD" }),
        ]);
        customRender(<ConnectorRequest />, { queryClient: true });
      });

      afterEach(cleanup);

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

      it("shows all environment names as options", async () => {
        await screen.findByRole("combobox", {
          name: "Environment *",
        });
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
        mockGetConnectorEnvironmentRequest.mockResolvedValue([
          createEnvironment({ id: "1", name: "DEV" }),
          createEnvironment({ id: "2", name: "TST" }),
          createEnvironment({ id: "3", name: "PROD" }),
        ]);
        customRender(<ConnectorRequest />, { queryClient: true });
      });

      afterEach(cleanup);

      describe("when 'PROD' option is clicked", () => {
        afterEach(cleanup);

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

  describe("Connector name", () => {
    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);
      customRender(
        <AquariumContext>
          <ConnectorRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });

    afterEach(cleanup);

    it("validates that connector name is at least 5 characters", async () => {
      const expectedErrorMsg = "Connector name must be at least 5 characters";
      const connectorNameInput = screen.getByLabelText(/Connector name/);
      await user.type(connectorNameInput, "test{tab}");
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      await user.clear(connectorNameInput);
      await user.type(connectorNameInput, "test-foobar{tab}");
      expect(errorMessage).not.toBeVisible();
    });
  });

  describe("Configuration", () => {
    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);
      customRender(
        <AquariumContext>
          <ConnectorRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });

    afterEach(cleanup);

    it("checks that config is JSON", async () => {
      const expectedErrorMsg = "Must be valid JSON";
      const mockedAdvancedConfig = screen.getByTestId(
        "connector-request-config"
      );
      await user.clear(mockedAdvancedConfig);
      await user.type(mockedAdvancedConfig, ":");
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      expect(mockedAdvancedConfig).toHaveDisplayValue(":");
    });

    it("checks that config is a JSON object", async () => {
      const expectedErrorMsg = "Must be a JSON Object";
      const mockedAdvancedConfig = screen.getByTestId(
        "connector-request-config"
      );
      await user.type(mockedAdvancedConfig, "[[]");
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      expect(mockedAdvancedConfig).toHaveDisplayValue(JSON.stringify([]));
    });

    it("checks that config includes tasks.max", async () => {
      const expectedErrorMsg = 'Missing "tasks.max" configuration property.';
      const mockedAdvancedConfig = screen.getByTestId(
        "connector-request-config"
      );
      await user.type(
        mockedAdvancedConfig,
        '{{ "connector.class": "test", "topics": "test" }'
      );
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      expect(mockedAdvancedConfig).toHaveDisplayValue(
        '{ "connector.class": "test", "topics": "test" }'
      );
    });

    it("checks that config includes connector.class", async () => {
      const expectedErrorMsg =
        'Missing "connector.class" configuration property.';
      const mockedAdvancedConfig = screen.getByTestId(
        "connector-request-config"
      );
      await user.type(
        mockedAdvancedConfig,
        '{{ "topics": "test", "tasks.max": 10 }'
      );
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      expect(mockedAdvancedConfig).toHaveDisplayValue(
        '{ "topics": "test", "tasks.max": 10 }'
      );
    });

    it("checks that config includes topics or topics.regex", async () => {
      const expectedErrorMsg =
        'Missing "topics" or "topics.regex" configuration property.';
      const mockedAdvancedConfig = screen.getByTestId(
        "connector-request-config"
      );
      await user.type(
        mockedAdvancedConfig,
        '{{ "connector.class": "test", "tasks.max": 10 }'
      );
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      expect(mockedAdvancedConfig).toHaveDisplayValue(
        '{ "connector.class": "test", "tasks.max": 10 }'
      );
    });
  });

  describe("Description", () => {
    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);
      customRender(
        <AquariumContext>
          <ConnectorRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });

    afterEach(cleanup);

    it("validates that connector description is at least 5 characters", async () => {
      const expectedErrorMsg =
        "Connector description must be at least 5 characters";
      const connectorNameInput = screen.getByLabelText(/Connector description/);
      await user.type(connectorNameInput, "test{tab}");
      const errorMessage = await screen.findByText(expectedErrorMsg);
      expect(errorMessage).toBeVisible();
      await user.clear(connectorNameInput);
      await user.type(connectorNameInput, "test-foobar{tab}");
      expect(errorMessage).not.toBeVisible();
    });
  });

  describe("Message for approval", () => {
    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);
      customRender(
        <AquariumContext>
          <ConnectorRequest />
        </AquariumContext>,
        { queryClient: true }
      );
    });

    afterEach(cleanup);

    it("can be optionally provided", async () => {
      const remarksInput = screen.getByLabelText("Message for approval");
      await user.clear(remarksInput);
      await user.type(remarksInput, "test");
      expect(remarksInput).toHaveDisplayValue("test");
    });
  });

  describe("form submission", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);
      customRender(
        <AquariumContext>
          <ConnectorRequest />
        </AquariumContext>,
        { queryClient: true }
      );

      await waitFor(() =>
        screen.getByRole("combobox", {
          name: "Environment *",
        })
      );

      await user.selectOptions(
        screen.getByRole("combobox", {
          name: "Environment *",
        }),
        "DEV"
      );
      await user.clear(screen.getByLabelText("Connector name*"));
      await user.type(
        screen.getByLabelText("Connector name*"),
        "this-is-connector-name{tab}"
      );
      await user.type(
        screen.getByTestId("connector-request-config"),
        '{{ "connector.class": "test", "tasks.max": 10, "topics": "test" }'
      );
      await user.type(
        screen.getByLabelText(/Connector description/),
        "testing"
      );
    });

    afterEach(() => {
      vi.clearAllMocks();
      cleanup();
    });

    describe("handles an error from the api", () => {
      beforeEach(() => {
        mockCreateConnectorRequest.mockRejectedValue({
          success: false,
          message: "Something went wrong",
        });
      });

      afterEach(() => {
        vi.clearAllMocks();
        cleanup();
      });

      it("renders an error message", async () => {
        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );
        await waitFor(() =>
          expect(createConnectorRequest).toHaveBeenCalledTimes(1)
        );
        expect(createConnectorRequest).toHaveBeenCalledWith({
          connectorConfig:
            '{ "connector.class": "test", "tasks.max": 10, "topics": "test" }',
          connectorName: "this-is-connector-name",
          description: "testing",
          environment: "1",
          remarks: "",
        });
        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Something went wrong");
      });
    });

    describe("enables user to create a new connector request", () => {
      beforeEach(async () => {
        mockCreateConnectorRequest.mockResolvedValue({
          success: true,
          message: "ok",
        });
      });

      afterEach(() => cleanup());

      it("creates a new connector request when input was valid", async () => {
        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        expect(createConnectorRequest).toHaveBeenCalledTimes(1);
        expect(createConnectorRequest).toHaveBeenCalledWith({
          connectorConfig:
            '{ "connector.class": "test", "tasks.max": 10, "topics": "test" }',
          connectorName: "this-is-connector-name",
          description: "testing",
          environment: "1",
          remarks: "",
        });
      });

      it("errors and does not create a new connector request when input was invalid", async () => {
        await user.clear(screen.getByLabelText("Connector name*"));

        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        await waitFor(() =>
          expect(
            screen.getByText("Connector name must be at least 5 characters")
          ).toBeVisible()
        );

        expect(createConnectorRequest).not.toHaveBeenCalled();
        expect(
          screen.getByRole("button", { name: "Submit request" })
        ).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        expect(createConnectorRequest).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Connector request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        await user.click(
          screen.getByRole("button", { name: "Submit request" })
        );

        expect(createConnectorRequest).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedUsedNavigate).toHaveBeenCalledWith(
          "/requests/connectors?status=CREATED"
        );
      });
    });
  });
  describe("enables user to cancel the form input", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ id: "1", name: "DEV" }),
        createEnvironment({ id: "2", name: "TST" }),
        createEnvironment({ id: "3", name: "PROD" }),
      ]);

      customRender(
        <AquariumContext>
          <ConnectorRequest />
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
      vi.clearAllMocks();
      cleanup();
    });

    it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
      const form = screen.getByRole("form", {
        name: `Request a new connector`,
      });

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });

    it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
      const form = screen.getByRole("form", {
        name: `Request a new connector`,
      });

      const nameInput = screen.getByRole("textbox", {
        name: "Connector name *",
      });
      await userEvent.type(nameInput, "Some name");

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);
      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
      expect(dialog).toHaveTextContent("Cancel connector request?");
      expect(dialog).toHaveTextContent(
        "Do you want to cancel this request? The data added will be lost."
      );

      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });

    it("brings the user back to the form when they do not cancel", async () => {
      const form = screen.getByRole("form", {
        name: `Request a new connector`,
      });

      const nameInput = screen.getByRole("textbox", {
        name: "Connector name *",
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
      const form = screen.getByRole("form", {
        name: `Request a new connector`,
      });

      const nameInput = screen.getByRole("textbox", {
        name: "Connector name *",
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
