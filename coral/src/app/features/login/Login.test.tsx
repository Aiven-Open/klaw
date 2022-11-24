import Login from "src/app/features/login/Login";
import { cleanup, screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";

describe("Login", () => {
  describe("renders all necessary elements", () => {
    beforeAll(() => {
      renderWithQueryClient(<Login />);
    });
    afterAll(cleanup);

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
