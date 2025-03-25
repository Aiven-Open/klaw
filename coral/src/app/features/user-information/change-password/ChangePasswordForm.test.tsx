import { cleanup, screen, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { ChangePasswordForm } from "src/app/features/user-information/change-password/ChangePasswordForm";
import { changePassword } from "src/domain/user/user-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/user/user-api");
const mockChangePassword = changePassword as jest.MockedFunction<
  typeof changePassword
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

describe("<ChangePasswordForm />", () => {
  describe("renders all fields and submit button", () => {
    beforeAll(() => {
      customRender(<ChangePasswordForm />, {
        queryClient: true,
        aquariumContext: true,
      });
    });
    afterAll(cleanup);

    it("shows correct elements", () => {
      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      expect(passwordField).toBeVisible();
      expect(confirmPasswordField).toBeVisible();
      expect(submitButton).toBeEnabled();
    });

    it("does not open confirm modal if fields are empty", async () => {
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.click(submitButton);

      const confirmModal = screen.queryByRole("dialog", {
        name: "Confirm password change?",
      });

      expect(confirmModal).not.toBeInTheDocument();
      expect(mockChangePassword).not.toHaveBeenCalled();
    });
  });

  describe("submits form when data is valid", () => {
    beforeEach(() => {
      mockChangePassword.mockResolvedValue({
        success: true,
        message: "success",
      });

      customRender(<ChangePasswordForm />, {
        queryClient: true,
        aquariumContext: true,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("sends a change password request", async () => {
      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.type(passwordField, "newPassword321@#");
      await userEvent.type(confirmPasswordField, "newPassword321@#");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const confirmModal = screen.getByRole("dialog", {
        name: "Confirm password change?",
      });
      const continueButton = screen.getByRole("button", {
        name: "Change password",
      });
      const cancelButton = screen.getByRole("button", {
        name: "Cancel password change",
      });

      expect(confirmModal).toBeVisible();
      expect(continueButton).toBeEnabled();
      expect(cancelButton).toBeEnabled();

      await userEvent.click(continueButton);

      expect(mockChangePassword).toHaveBeenCalledWith({
        pwd: "newPassword321@#",
        repeatPwd: "newPassword321@#",
      });

      expect(confirmModal).not.toBeInTheDocument();
      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "Password successfully changed",
        position: "bottom-left",
        variant: "default",
      });
    });

    it("cancels password change request", async () => {
      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.type(passwordField, "newPassword321@#");
      await userEvent.type(confirmPasswordField, "newPassword321@#");
      await userEvent.click(submitButton);

      const confirmModal = screen.getByRole("dialog", {
        name: "Confirm password change?",
      });
      const continueButton = screen.getByRole("button", {
        name: "Change password",
      });
      const cancelButton = screen.getByRole("button", {
        name: "Cancel password change",
      });

      expect(confirmModal).toBeVisible();
      expect(continueButton).toBeEnabled();
      expect(cancelButton).toBeEnabled();

      await userEvent.click(cancelButton);

      expect(confirmModal).not.toBeInTheDocument();

      expect(mockChangePassword).not.toHaveBeenCalledWith({
        pwd: "newPassword321@#",
        repeatPwd: "newPassword321@#",
      });
    });
  });

  describe("handle errors", () => {
    beforeEach(() => {
      mockChangePassword.mockRejectedValue({
        success: false,
        message: "error",
      });

      customRender(<ChangePasswordForm />, {
        queryClient: true,
        aquariumContext: true,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows an error when password is too short", async () => {
      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.type(passwordField, "123");
      await userEvent.type(confirmPasswordField, "123");
      await userEvent.tab();

      const errors = screen.getAllByText("Must be 8 or more characters long");

      expect(errors).toHaveLength(1);

      await userEvent.click(submitButton);

      const confirmModal = screen.queryByRole("dialog", {
        name: "Confirm password change?",
      });

      expect(confirmModal).not.toBeInTheDocument();
      expect(mockChangePassword).not.toHaveBeenCalled();
    });

    it("shows an error when password don't match", async () => {
      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.type(passwordField, "newPassword321@#");
      await userEvent.type(confirmPasswordField, "NOTTHESAME");
      await userEvent.tab();

      const error = screen.getByText("Passwords don't match");

      expect(confirmPasswordField).toBeInvalid();
      expect(error).toBeVisible();

      await userEvent.click(submitButton);

      const confirmModal = screen.queryByRole("dialog", {
        name: "Confirm password change?",
      });

      expect(confirmModal).not.toBeInTheDocument();
      expect(mockChangePassword).not.toHaveBeenCalled();
    });

    it("closes modal and render error when call to change password endpoint returns an error", async () => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);

      const form = screen.getByRole("form", {
        name: "Change your password by entering a new password",
      });
      const passwordField = within(form).getByLabelText("New password *");
      const confirmPasswordField = within(form).getByLabelText(
        "Confirm new password"
      );
      const submitButton = screen.getByRole("button", {
        name: "Update password",
      });

      await userEvent.type(passwordField, "newPassword321@#");
      await userEvent.type(confirmPasswordField, "newPassword321@#");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const confirmModal = screen.getByRole("dialog", {
        name: "Confirm password change?",
      });
      const continueButton = screen.getByRole("button", {
        name: "Change password",
      });
      const cancelButton = screen.getByRole("button", {
        name: "Cancel password change",
      });

      expect(confirmModal).toBeVisible();
      expect(continueButton).toBeEnabled();
      expect(cancelButton).toBeEnabled();

      await userEvent.click(continueButton);

      expect(mockChangePassword).toHaveBeenCalledWith({
        pwd: "newPassword321@#",
        repeatPwd: "newPassword321@#",
      });

      expect(confirmModal).not.toBeInTheDocument();

      const errorBox = screen.getByRole("alert");

      expect(errorBox).toBeVisible();
      expect(errorBox).toHaveTextContent("error");
      expect(console.error).toHaveBeenCalledWith({
        success: false,
        message: "error",
      });
    });
  });

  describe("shows information about password strength to user", () => {
    beforeEach(() => {
      customRender(<ChangePasswordForm />, {
        queryClient: true,
        aquariumContext: true,
      });
    });
    afterEach(cleanup);

    it("renders PasswordStrengthMeter and announces lowercase rule as met when typing a lowercase password", async () => {
      const passwordField = screen.getByLabelText("New password *");

      await userEvent.type(passwordField, "abcd");

      const announcement = await screen.findByRole("alert");

      expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
      expect(announcement).toHaveTextContent(/8\+ characters: Not met/i);
      expect(announcement).toHaveTextContent(/Uppercase letter: Not met/i);
      expect(announcement).toHaveTextContent(/Number: Not met/i);
      expect(announcement).toHaveTextContent(/Special character: Not met/i);
    });

    it("removes password strength announcement when user leaves field", async () => {
      const passwordField = screen.getByLabelText("New password *");

      await userEvent.type(passwordField, "abcd");

      const announcement = await screen.findByRole("alert");

      expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);

      await userEvent.tab();

      expect(announcement).not.toBeVisible();
    });
  });
});
