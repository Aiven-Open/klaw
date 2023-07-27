import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import ConnectorEditRequest from "src/app/features/connectors/request/ConnectorEditRequest";
import { editConnector, getConnectorDetailsPerEnv } from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import * as ReactQuery from "@tanstack/react-query";

const mockError = {
  isError: true,
  error: {
    success: false,
    message: "Failure. Not authorized to request topic for this environment.",
  },
};

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/connector/connector-api.ts");

const CONNECTOR_NAME = "test-connector";
const ENV_ID = "4";

const testConnectorDetailsPerEnvResponse = {
  connectorExists: true,
  connectorId: 1005,
  connectorContents: {
    runningTasks: 0,
    failedTasks: 0,
    environmentId: "4",
    teamName: "Ospo",
    teamId: 0,
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
    connectorOwner: false,
    highestEnv: false,
    hasOpenRequest: false,
    connectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "Aindriu45",\n  "topic" : "NewTopic",\n  "topics.regex" : "*"\n}',
    environmentName: "DEV",
    description: "Connector description",
    // Placeholder,  these are marked as required but are not actually returned by getConnectorDetailsPerEnv
    connectorId: -1,
    connectorName: "placeholder",
    connectorStatus: "placeholder",
  },
};

const mockGetConnectorDetailsPerEnv =
  getConnectorDetailsPerEnv as jest.MockedFunction<
    typeof getConnectorDetailsPerEnv
  >;

const mockEditConnector = editConnector as jest.MockedFunction<
  typeof editConnector
>;

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

describe("<ConnectorEditRequest />", () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe("ConnectorEditRequest fields", () => {
    beforeEach(() => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue(
        testConnectorDetailsPerEnvResponse
      );
      customRender(
        <Routes>
          <Route
            path="/connector/:connectorName/request-update"
            element={
              <AquariumContext>
                <ConnectorEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/connector/${CONNECTOR_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    describe("Renders disabled fields with correct values", () => {
      it("shows a disabled required select element for 'Environment' with correct value", async () => {
        const select = await screen.findByRole("combobox", {
          name: "Environment *",
        });
        expect(select).toBeDisabled();
        expect(select).toBeRequired();
        expect(select).toHaveDisplayValue(
          testConnectorDetailsPerEnvResponse.connectorContents.environmentName
        );
      });

      it("shows a readOnly required input element for 'Connector name' with correct value", async () => {
        const input = await screen.findByRole("textbox", {
          name: "Connector name *",
        });
        expect(input).toHaveAttribute("readonly");
        expect(input).toBeRequired();
        expect(input).toHaveDisplayValue(CONNECTOR_NAME);
      });
    });

    describe("Renders enabled field with correct values", () => {
      it("should render 'Connector configuration' with correct default value", async () => {
        const mockedAdvancedConfig = screen.getByTestId(
          "connector-request-config"
        );

        expect(mockedAdvancedConfig).toBeEnabled();
        await waitFor(() =>
          expect(mockedAdvancedConfig).toHaveDisplayValue(
            testConnectorDetailsPerEnvResponse.connectorContents.connectorConfig
          )
        );
      });
    });

    it("should render 'Description' with correct default value", async () => {
      const connectorNameInput = screen.getByRole("textbox", {
        name: "Connector description *",
      });
      await waitFor(() =>
        expect(connectorNameInput).toHaveDisplayValue(
          testConnectorDetailsPerEnvResponse.connectorContents.description
        )
      );
    });

    it("should render 'Message for approval' with no default value", async () => {
      const remarksInput = screen.getByRole("textbox", {
        name: "Message for approval",
      });
      await user.clear(remarksInput);
      await user.type(remarksInput, "test");
      expect(remarksInput).toHaveDisplayValue("test");
    });

    it("shows a notification and does not submit when user has not changed topic data", async () => {
      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditConnector).not.toHaveBeenCalled();

      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "No changes were made to the connector.",
        position: "bottom-left",
        variant: "default",
      });
    });
  });

  describe("ConnectorEditRequest form submission", () => {
    beforeEach(() => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue(
        testConnectorDetailsPerEnvResponse
      );
      customRender(
        <Routes>
          <Route
            path="/connector/:connectorName/request-update"
            element={
              <AquariumContext>
                <ConnectorEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/connector/${CONNECTOR_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a notification and does not submit when user has not changed connector data", async () => {
      await userEvent.click(
        screen.getByRole("button", { name: "Submit update request" })
      );

      expect(mockEditConnector).not.toHaveBeenCalled();

      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "No changes were made to the connector.",
        position: "bottom-left",
        variant: "default",
      });
    });

    describe("enables user to create a new connector update request", () => {
      beforeEach(async () => {
        mockEditConnector.mockResolvedValue({
          success: true,
          message: "ok",
        });
      });

      afterEach(() => {
        mockedUseToast.mockReset();
        cleanup();
      });

      it("creates a new connector update request when input was valid", async () => {
        await user.type(
          screen.getByRole("textbox", {
            name: "Connector description *",
          }),
          "hello"
        );
        await user.click(
          screen.getByRole("button", { name: "Submit update request" })
        );

        expect(editConnector).toHaveBeenCalledTimes(1);
        expect(editConnector).toHaveBeenCalledWith({
          connectorConfig:
            testConnectorDetailsPerEnvResponse.connectorContents
              .connectorConfig,
          connectorName: CONNECTOR_NAME,
          description: `${testConnectorDetailsPerEnvResponse.connectorContents.description}hello`,
          environment: ENV_ID,
          remarks: "",
        });
        expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
        await waitFor(() => expect(mockedUseToast).toHaveBeenCalled());
      });

      it("errors and does not create a new connector request when input was invalid", async () => {
        await waitFor(() =>
          expect(
            screen.getByRole("textbox", { name: "Connector description *" })
          ).toHaveDisplayValue("Connector description")
        );

        await user.clear(
          screen.getByRole("textbox", { name: "Connector description *" })
        );

        expect(
          screen.getByRole("textbox", { name: "Connector description *" })
        ).toHaveDisplayValue("");

        await user.click(
          screen.getByRole("button", { name: "Submit update request" })
        );

        await waitFor(() =>
          expect(
            screen.getByText(
              "Connector description must be at least 5 characters"
            )
          ).toBeVisible()
        );

        expect(editConnector).not.toHaveBeenCalled();
        expect(mockedUseToast).not.toHaveBeenCalled();
        expect(
          screen.getByRole("button", { name: "Submit update request" })
        ).toBeEnabled();
      });
    });
  });

  describe("enables user to cancel the form input", () => {
    beforeEach(async () => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue(
        testConnectorDetailsPerEnvResponse
      );
      customRender(
        <Routes>
          <Route
            path="/connector/:connectorName/request-update"
            element={
              <AquariumContext>
                <ConnectorEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/connector/${CONNECTOR_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
      const form = screen.getByRole("form", {
        name: `Request connector update`,
      });

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(button);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });

    it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
      const button = screen.getByRole("button", {
        name: "Cancel",
      });

      await user.type(
        screen.getByRole("textbox", {
          name: "Connector description *",
        }),
        "hello"
      );

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
      await user.type(
        screen.getByRole("textbox", {
          name: "Connector description *",
        }),
        "hello"
      );

      const button = screen.getByRole("button", {
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
      await user.type(
        screen.getByRole("textbox", {
          name: "Connector description *",
        }),
        "hello"
      );

      const button = screen.getByRole("button", {
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

  describe("shows an alert message when new topic update request was not successful", () => {
    beforeEach(() => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue(
        testConnectorDetailsPerEnvResponse
      );
      jest
        .spyOn(ReactQuery, "useMutation")
        .mockImplementation(jest.fn().mockReturnValue(mockError));
      customRender(
        <Routes>
          <Route
            path="/connector/:connectorName/request-update"
            element={
              <AquariumContext>
                <ConnectorEditRequest />
              </AquariumContext>
            }
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: `/connector/${CONNECTOR_NAME}/request-update?env=${ENV_ID}`,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("render an alert when server rejects update request", async () => {
      await waitFor(() =>
        expect(screen.getByRole("alert")).toHaveTextContent(
          mockError.error.message
        )
      );
    });
  });
});
