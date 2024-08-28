import { cleanup, screen } from "@testing-library/react/pure";
import { userEvent } from "@testing-library/user-event";
import Connectors from "src/app/pages/connectors";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";

// Mocking the BrowseConnectors component
// so this test only confirms the correct component
// (that is already tested) is rendered.
// eslint-disable-next-line react/display-name
jest.mock("src/app/features/connectors/browse/BrowseConnectors", () => () => (
  <div data-testid="mocked-BrowseConnectors" />
));

const mockedNavigator = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigator,
}));

let mockAuthUser = testAuthUser;
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser,
}));

describe("Connectors", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("renders default view with data for users", () => {
    beforeAll(() => {
      mockAuthUser = { ...testAuthUser, userrole: "USER" };

      customRender(<Connectors />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
    });

    afterAll(() => {
      cleanup();
    });

    it("shows a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Connectors",
      });

      expect(headline).toBeVisible();
    });

    it("renders 'Request new connector' button in heading", async () => {
      const button = screen.getByRole("button", {
        name: "Request new connector",
      });

      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("navigates to '/connectors/request' when user clicks the button 'Request new connector'", async () => {
      const button = screen.getByRole("button", {
        name: "Request new connector",
      });

      await userEvent.click(button);

      expect(mockedNavigator).toHaveBeenCalledWith("/connectors/request");
    });

    it("navigates to '/connectors/request' when user presses Enter while 'Request new topic' button is focused", async () => {
      const button = screen.getByRole("button", {
        name: "Request new connector",
      });

      await tabNavigateTo({ targetElement: button });

      await userEvent.keyboard("{Enter}");

      expect(mockedNavigator).toHaveBeenCalledWith("/connectors/request");
    });

    it("renders the BrowserConnectors component rendering the table", async () => {
      const component = screen.getByTestId("mocked-BrowseConnectors");

      expect(component).toBeVisible();
    });
  });

  describe("does not render the button to request a new topic for superadmin", () => {
    beforeAll(() => {
      mockAuthUser = { ...testAuthUser, userrole: "SUPERADMIN" };

      customRender(<Connectors />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
    });

    afterAll(() => {
      cleanup();
    });

    it("shows the same headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Connectors",
      });

      expect(headline).toBeVisible();
    });
    it("renders the BrowserConnectors component rendering the table", async () => {
      const component = screen.getByTestId("mocked-BrowseConnectors");

      expect(component).toBeVisible();
    });

    it("does not renders 'Request new connector' button", async () => {
      const button = screen.queryByText("Request new connector");

      expect(button).not.toBeInTheDocument();
    });
  });
});
