import { Context as AquariumContext } from "@aivenio/aquarium";
import { ConnectorPromotionRequest } from "src/app/features/connectors/request/ConnectorPromotionRequest";
import { cleanup, waitFor, screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Route, Routes } from "react-router-dom";
import {
  Environment,
  getAllEnvironmentsForConnector,
} from "src/domain/environment";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import {
  ConnectorDetailsForEnv,
  getConnectorDetailsPerEnv,
  requestConnectorPromotion,
} from "src/domain/connector";
import { waitForElementToBeRemoved, within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";

jest.mock("src/domain/environment/environment-api.ts");
const mockGetConnectorEnvironmentRequest =
  getAllEnvironmentsForConnector as jest.MockedFunction<
    typeof getAllEnvironmentsForConnector
  >;

jest.mock("src/domain/connector/connector-api.ts");
const mockGetConnectorDetailsPerEnv =
  getConnectorDetailsPerEnv as jest.MockedFunction<
    typeof getConnectorDetailsPerEnv
  >;

const mockRequestConnectorPromotion =
  requestConnectorPromotion as jest.MockedFunction<
    typeof requestConnectorPromotion
  >;

const testConnectorName = "test-connector-name";
const defaultSourceEnv = "111";
const defaultTargetEnv = "999";
const defaultEnvironments: Environment[] = [
  createEnvironment({ name: "DEV", id: defaultSourceEnv }),
  createEnvironment({ name: "TST", id: defaultTargetEnv }),
];

const testConnectorConfig =
  '{\n"connector.class":"io.confluent.connect.storage.tools.SchemaSourceConnector",\n' +
  '  "tasks.max" : "1",\n  "name" : "test-connector-name",\n  "topic" : "NewTopic",\n  "topics.regex" : "*"\n}';
const testConnectorDetails: ConnectorDetailsForEnv = {
  connectorExists: true,
  connectorId: 1,
  connectorContents: {
    connectorName: testConnectorName,
    environmentId: defaultTargetEnv,
    connectorConfig: testConnectorConfig,
    description: "Just a description",
  } as ConnectorDetailsForEnv["connectorContents"],
};

function renderConnectorPromotionRequest({
  sourceEnv = defaultSourceEnv,
  targetEnv = defaultTargetEnv,
}: {
  sourceEnv?: string | null;
  targetEnv?: string | null;
}) {
  return customRender(
    <Routes>
      <Route
        path="/connector/:connectorName/request-promotion"
        element={
          <AquariumContext>
            <ConnectorPromotionRequest />
          </AquariumContext>
        }
      />
    </Routes>,
    {
      queryClient: true,
      memoryRouter: true,
      customRoutePath: `/connector/${testConnectorName}/request-promotion?${
        sourceEnv ? `sourceEnv=${sourceEnv}` : ""
      }${targetEnv ? `&targetEnv=${targetEnv}` : ""}`,
    }
  );
}

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

describe("ConnectorPromotionRequest", () => {
  const user = userEvent.setup();

  describe("handles passing of correct params via url", () => {
    beforeEach(() => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue({});
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("redirects user and shows error when there is no source env", () => {
      renderConnectorPromotionRequest({ sourceEnv: null });

      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({ message: "Missing url parameter" })
      );
      expect(mockedUsedNavigate).toHaveBeenCalledWith(
        "/connector/test-connector-name",
        { replace: true }
      );
    });

    it("redirects user and shows error when there is no target env", () => {
      renderConnectorPromotionRequest({ targetEnv: null });

      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({ message: "Missing url parameter" })
      );
      expect(mockedUsedNavigate).toHaveBeenCalledWith(
        "/connector/test-connector-name",
        { replace: true }
      );
    });
  });

  describe("loads and handles available environments", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue({});
      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches available environments on page load", () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorEnvironmentRequest).toHaveBeenCalledTimes(1);
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs and redirects user when sourceEnv does not exist", async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ name: "TST", id: defaultTargetEnv }),
      ]);

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorEnvironmentRequest).toHaveBeenCalledTimes(1);

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: `No source environment was found with ID ${defaultSourceEnv}`,
          })
        )
      );

      expect(mockedUsedNavigate).toHaveBeenCalledWith(
        "/connector/test-connector-name",
        { replace: true }
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs and redirects user when targetEnv does not exist", async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue([
        createEnvironment({ name: "DEV", id: defaultSourceEnv }),
      ]);

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorEnvironmentRequest).toHaveBeenCalledTimes(1);

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: `No target environment was found with ID ${defaultTargetEnv}`,
          })
        )
      );

      expect(mockedUsedNavigate).toHaveBeenCalledWith(
        "/connector/test-connector-name",
        { replace: true }
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs and redirects user when an error occurs with fetching", async () => {
      mockGetConnectorEnvironmentRequest.mockRejectedValue({
        message: "Oh no",
      });

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorEnvironmentRequest).toHaveBeenCalledTimes(1);

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: "Error while fetching available environments: Oh no",
          })
        )
      );

      expect(mockedUsedNavigate).toHaveBeenCalledWith(
        "/connector/test-connector-name",
        { replace: true }
      );
      expect(console.error).toHaveBeenCalledWith({ message: "Oh no" });
    });
  });

  describe("loads and handles connector details", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);

      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches connector details on load", () => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue({});

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorDetailsPerEnv).toHaveBeenCalledTimes(1);
      expect(mockGetConnectorDetailsPerEnv).toHaveBeenCalledWith({
        connectorName: testConnectorName,
        envSelected: defaultSourceEnv,
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs and redirects user when connector details does not exist", async () => {
      mockGetConnectorDetailsPerEnv.mockResolvedValue({});

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorDetailsPerEnv).toHaveBeenCalledTimes(1);

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: `No connector was found with name ${testConnectorName}`,
          })
        )
      );

      expect(mockedUsedNavigate).toHaveBeenCalledWith("/connector", {
        replace: true,
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs and redirects user when an error occurs with fetching", async () => {
      mockGetConnectorDetailsPerEnv.mockRejectedValue({
        message: "Oh no",
      });

      renderConnectorPromotionRequest({});

      expect(mockGetConnectorDetailsPerEnv).toHaveBeenCalledTimes(1);

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message:
              "Error while fetching connector test-connector-name: Oh no",
          })
        )
      );

      expect(mockedUsedNavigate).toHaveBeenCalledWith("/connector", {
        replace: true,
      });
      expect(console.error).toHaveBeenCalledWith({ message: "Oh no" });
    });
  });

  describe("renders form to request promotion", () => {
    beforeAll(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);

      renderConnectorPromotionRequest({});

      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a form", () => {
      const form = screen.getByRole("form", {
        name: "Request connector promotion",
      });

      expect(form).toBeVisible();
    });

    it("shows a prefilled, readonly select for environments", () => {
      const select = screen.getByRole("combobox", {
        name: "Environment (read-only)",
      });

      expect(select).toBeDisabled();
      expect(select).toHaveAttribute("aria-readonly", "true");
      expect(select).toHaveValue(defaultTargetEnv);
    });

    it("shows a prefilled, readonly text input for name", () => {
      const textInput = screen.getByRole("textbox", {
        name: "Connector name (read-only)",
      });

      expect(textInput).toBeVisible();
      expect(textInput).toHaveAttribute("readonly");
      expect(textInput).toHaveValue(testConnectorName);
    });

    it("shows a prefilled editor for configuration", () => {
      const editor = screen.getByTestId("connector-config");

      expect(editor).toBeVisible();
      expect(editor).toHaveDisplayValue(testConnectorConfig);
    });

    it("shows a prefilled, readonly textarea for description", () => {
      const textarea = screen.getByRole("textbox", {
        name: "Connector description (read-only)",
      });

      expect(textarea).toBeVisible();
      expect(textarea).toHaveAttribute("readonly");
      expect(textarea).toHaveValue("Just a description");
    });

    it("shows an optional textarea for user to add comment", () => {
      const textarea = screen.getByRole("textbox", {
        name: "Message for approval",
      });

      expect(textarea).toBeEnabled();
    });
  });

  describe("enables user to request promotion with no changes", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);
      mockRequestConnectorPromotion.mockResolvedValue({
        success: true,
        message: "yey",
      });

      renderConnectorPromotionRequest({});
      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("sends a request to promote a connector", async () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.click(submitButton);

      expect(mockRequestConnectorPromotion).toHaveBeenCalledWith({
        connectorName: testConnectorName,
        environment: defaultTargetEnv,
        description: "Just a description",
        connectorConfig: testConnectorConfig,
        remarks: "",
      });
    });

    it("shows success message and redirects user", async () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.click(submitButton);

      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: "Connector update request successfully created",
        })
      );
      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });
  });

  describe("enables user to request promotion updated configuration", () => {
    const updatedConfig =
      '{ "connector.class" : "someclass" , "tasks.max" : "1", "name" : "test-connector-name",' +
      ' "topic" : "my-nice-topic", "topics.regex" : "*" }';

    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);

      renderConnectorPromotionRequest({});
      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("updates existing configuration on user input", async () => {
      const editor = await screen.findByTestId("connector-config");

      await user.clear(editor);
      // first { is needed to avoid error from userEvent
      // (error was: Expected repeat modifier or release modifier or "}" but found """ )
      await user.type(editor, `{${updatedConfig}`);
      await user.tab();

      expect(editor).toHaveValue(updatedConfig);
    });

    it("requests the promotion with the updated configuration", async () => {
      const editor = await screen.findByTestId("connector-config");

      await user.clear(editor);
      // first { is needed to avoid error from userEvent
      // (error was: Expected repeat modifier or release modifier or "}" but found """ )
      await user.type(editor, `{${updatedConfig}`);
      await user.tab();

      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.click(submitButton);

      expect(mockRequestConnectorPromotion).toHaveBeenCalledWith({
        connectorName: testConnectorName,
        environment: defaultTargetEnv,
        description: "Just a description",
        connectorConfig: updatedConfig,
        remarks: "",
      });
    });
  });

  describe("enables user to request promotion with message for approval", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);

      renderConnectorPromotionRequest({});
      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("sends message for approval with promote request", async () => {
      const testMessage = "Please approve 🥺";
      const textarea = screen.getByRole("textbox", {
        name: "Message for approval",
      });
      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.type(textarea, testMessage);
      await user.click(submitButton);

      expect(mockRequestConnectorPromotion).toHaveBeenCalledWith({
        connectorName: testConnectorName,
        environment: defaultTargetEnv,
        description: "Just a description",
        connectorConfig: testConnectorConfig,
        remarks: testMessage,
      });
    });
  });

  describe("handles errors when requesting promotion", () => {
    const testErrorMessage = "OH NO 😭";
    const originalConsoleError = console.error;

    beforeEach(async () => {
      console.error = jest.fn();
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);
      mockRequestConnectorPromotion.mockRejectedValue({
        success: false,
        message: testErrorMessage,
      });

      renderConnectorPromotionRequest({});
      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.clearAllMocks();
      cleanup();
    });

    it("shows an error message to the user", async () => {
      const anyAlert = screen.queryByRole("alert");
      expect(anyAlert).not.toBeInTheDocument();

      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.click(submitButton);

      const alertError = screen.getByRole("alert");

      expect(alertError).toBeVisible();
      expect(alertError).toHaveTextContent(testErrorMessage);
      expect(console.error).toHaveBeenCalledWith({
        message: testErrorMessage,
        success: false,
      });
    });

    it("does not show success message and redirects user", async () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit promotion request",
      });

      await user.click(submitButton);

      expect(mockedUseToast).not.toHaveBeenCalled();
      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });
  });

  describe("enables user to cancel promotion", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(defaultEnvironments);
      mockGetConnectorDetailsPerEnv.mockResolvedValue(testConnectorDetails);
      mockRequestConnectorPromotion.mockResolvedValue({
        success: true,
        message: "yey",
      });

      renderConnectorPromotionRequest({});
      await waitForElementToBeRemoved(screen.getByText("Form is loading."));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("redirects user when they didn't change anything", async () => {
      const cancelButton = screen.getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
      expect(mockRequestConnectorPromotion).not.toHaveBeenCalled();
    });

    it("shows dialog to user user when they had changes", async () => {
      const textarea = screen.getByRole("textbox", {
        name: "Message for approval",
      });
      await user.type(textarea, "this is a message");

      const cancelButton = screen.getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();
      expect(dialog).toHaveTextContent("Cancel connector request?");

      expect(mockedUsedNavigate).not.toHaveBeenCalled();
      expect(mockRequestConnectorPromotion).not.toHaveBeenCalled();
    });

    it("closes modal and shows form again when user wants to continue with request", async () => {
      const textarea = screen.getByRole("textbox", {
        name: "Message for approval",
      });
      await user.type(textarea, "this is a message");

      const cancelButton = screen.getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      const dialog = screen.getByRole("dialog");
      const continueButton = within(dialog).getByRole("button", {
        name: "Continue with request",
      });

      await user.click(continueButton);

      expect(dialog).not.toBeVisible();
      expect(mockedUsedNavigate).not.toHaveBeenCalled();
      expect(mockRequestConnectorPromotion).not.toHaveBeenCalled();
    });

    it("cancels process and redirects user when they confirm cancel", async () => {
      const textarea = screen.getByRole("textbox", {
        name: "Message for approval",
      });
      await user.type(textarea, "this is a message");

      const cancelButton = screen.getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      const dialog = screen.getByRole("dialog");
      const confirmCancel = within(dialog).getByRole("button", {
        name: "Cancel request",
      });

      await user.click(confirmCancel);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
      expect(mockRequestConnectorPromotion).not.toHaveBeenCalled();
    });
  });
});
