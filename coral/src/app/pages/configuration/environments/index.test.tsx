import { screen } from "@testing-library/react/pure";
import EnvironmentsPage from "src/app/pages/configuration/environments";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("Environments page", () => {
  describe("renders Environments page with correct text", () => {
    beforeAll(() => {
      customRender(<EnvironmentsPage />, { memoryRouter: true });
    });

    it("renders a headline", () => {
      const headline = screen.getByRole("heading", {
        name: "Environments",
      });

      expect(headline).toBeVisible();
    });
  });
});
