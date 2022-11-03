import Login from "src/app/pages/Login";
import { screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";

describe("Login", () => {
  beforeEach(() => {
    renderWithQueryClient(<Login />);
  });

  describe("renders all necessary elements", () => {
    it("shows a headline", () => {
      const headline = screen.getByRole("heading", { name: "Login page" });
      expect(headline).toBeVisible();
    });

    it("shows the login form", () => {
      const form = screen.getByRole("textbox", { name: /Username/ });
      expect(form).toBeEnabled();
    });
  });
});
