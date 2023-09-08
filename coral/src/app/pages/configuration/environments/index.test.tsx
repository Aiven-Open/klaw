import { cleanup, screen } from "@testing-library/react/pure";
import EnvironmentsPage from "src/app/pages/configuration/environments";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockMatches = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useMatches: () => mockMatches(),
}));

describe("Environments page", () => {
  describe("renders Environments page with correct text", () => {
    beforeAll(() => {
      mockMatches.mockImplementation(() => [
        {
          id: "ENVIRONMENTS_TAB_ENUM_kafka",
        },
      ]);

      customRender(<EnvironmentsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(cleanup);

    it("renders a headline", () => {
      const headline = screen.getByRole("heading", {
        name: "Environments",
      });

      expect(headline).toBeVisible();
    });
  });
});
