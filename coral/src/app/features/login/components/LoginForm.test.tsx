import { LoginForm } from "src/app/features/login/components/LoginForm";
import { screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { server } from "src/services/api-mocks/server";
import {
  mockUserAuthRequest,
  correctUsername,
} from "src/domain/auth-user/auth-user-api.msw";

const successfulLoginMessage = "Login successful 🎉";
const loginDataWrong = "Something went wrong 😞";

describe("Login", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockUserAuthRequest(server);
      customRender(<LoginForm />, { queryClient: true });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
      server.resetHandlers();
    });

    it("shows an input field for username", () => {
      const input = screen.getByRole("textbox", { name: /Username/ });
      expect(input).toBeEnabled();
    });

    it("shows an input field for password", () => {
      // For security reasons, there is no role password
      // -> https://github.com/w3c/aria/issues/166
      // the recommended way to query is using byLabel
      const input = screen.getByLabelText(/Password/);
      expect(input).toBeEnabled();
    });

    it("shows a disabled submit button for the form", () => {
      const button = screen.getByRole("button", { name: "Submit" });
      expect(button).toBeEnabled();
    });
  });

  describe("provides form control", () => {
    beforeEach(() => {
      mockUserAuthRequest(server);
      customRender(<LoginForm />, { queryClient: true });
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
      server.resetHandlers();
    });

    it("user can not submit form if not all inputs are filled", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, correctUsername);
      await userEvent.tab();
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(
          screen.queryByText(successfulLoginMessage)
        ).not.toBeInTheDocument();
      });
    });

    it("user gets an error message telling them what fields are missing", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, correctUsername);
      await userEvent.tab();
      await userEvent.click(submitButton);

      const passwordInput = screen.getByLabelText(/Password/);
      const errorMessage = await screen.findByText("Password is required");

      expect(passwordInput).toBeInvalid();
      expect(errorMessage).toBeVisible();
    });

    it("user can fill username and password and submit form", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const passwordInput = screen.getByLabelText(/Password/);
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, correctUsername);
      await userEvent.tab();
      await userEvent.type(passwordInput, "password123");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const successMessage = await screen.findByText(successfulLoginMessage);
      expect(successMessage).toBeVisible();
    });

    it("user sees error message if username or password is wrong", async () => {
      const originalConsoleError = console.error;
      console.error = jest.fn();

      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const passwordInput = screen.getByLabelText(/Password/);
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, "wrong username in this test");
      await userEvent.tab();
      await userEvent.type(passwordInput, "password123");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const wrongDataMessage = await screen.findByText(loginDataWrong);
      expect(wrongDataMessage).toBeVisible();
      expect(console.error).toHaveBeenCalled();
      console.error = originalConsoleError;
    });
  });
});
