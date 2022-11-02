import Login from "src/app/pages/Login";
import { render, cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("Login", () => {
  beforeEach(() => {
    render(<Login />);
  });

  afterEach(() => {
    cleanup();
  });

  describe("renders all necessary elements", () => {
    it("shows a headline", () => {
      const headline = screen.getByRole("heading", { name: "Login page" });
      expect(headline).toBeVisible();
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
      expect(button).toBeDisabled();
    });
  });

  describe("provides form control", () => {
    it("user can fill username and password and submit form", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const passwordInput = screen.getByLabelText(/Password/);
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, "my username");
      await userEvent.tab();
      await userEvent.type(passwordInput, "badpassword123");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const successMessage = await screen.findByText("Login successful!");
      expect(successMessage).toBeVisible();
    });

    it("user can not submit form if not all inputs are filled", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, "my username");
      await userEvent.tab();
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByText("Login successful!")).not.toBeInTheDocument();
      });
    });

    it("user gets an error message telling them what fields are missing", async () => {
      const usernameInput = screen.getByRole("textbox", { name: /Username/ });
      const submitButton = screen.getByRole("button", { name: "Submit" });

      await userEvent.type(usernameInput, "my username");
      await userEvent.tab();
      await userEvent.click(submitButton);

      const passwordInput = screen.getByLabelText(/Password/);
      const errorMessage = await screen.findByText("Password is required");

      expect(passwordInput).toBeInvalid();
      expect(errorMessage).toBeVisible();
    });
  });
});
