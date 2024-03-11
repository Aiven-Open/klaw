import { cleanup, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { ProfileDropdown } from "src/app/layout/header/ProfileDropdown";
import { AuthUser, logoutUser } from "src/domain/auth-user";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockLogoutUser = logoutUser as jest.MockedFunction<typeof logoutUser>;
const mockedNavigate = jest.fn();
const mockAuthUser = jest.fn();
const mockToast = jest.fn();
const mockDismiss = jest.fn();

jest.mock("src/domain/auth-user/auth-user-api");
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToastContext: () => [mockToast, mockDismiss],
}));

const menuItems = [
  {
    angularPath: "/myProfile",
    path: "/user/profile",
    name: "User profile",
  },
  {
    angularPath: "/changePwd",
    path: "/user/change-password",
    name: "Change password",
  },
  {
    angularPath: "/tenantInfo",
    path: "/user/tenant-info",
    name: "Tenant information",
  },
];
describe("ProfileDropdown", () => {
  const testUser: AuthUser = {
    teamname: "new team",
    teamId: "1234",
    username: "It is a me",
    userrole: "USER",
    canSwitchTeams: "false",
    totalTeamTopics: 0,
    totalOrgTopics: 0,
    permissions: {
      canShutdownKw: false,
      canUpdatePermissions: false,
      addEditRoles: false,
      viewTopics: false,
      requestItems: false,
      viewKafkaConnect: false,
      syncBackTopics: false,
      syncBackSchemas: false,
      syncBackAcls: false,
      updateServerConfig: false,
      showServerConfigEnvProperties: false,
      addUser: false,
      addTeams: false,
      syncTopicsAcls: false,
      syncConnectors: false,
      manageConnectors: false,
      syncSchemas: false,
      approveAtleastOneRequest: false,
      approveDeclineTopics: false,
      approveDeclineOperationalReqs: false,
      approveDeclineSubscriptions: false,
      approveDeclineSchemas: false,
      approveDeclineConnectors: false,
      showAddDeleteTenants: false,
      addDeleteEditClusters: false,
      addDeleteEditEnvs: false,
    },
  };

  const user = userEvent.setup();

  describe("renders all necessary elements when dropdown is closed", () => {
    beforeAll(() => {
      mockAuthUser.mockReturnValue(testUser);
      customRender(<ProfileDropdown />, { memoryRouter: true });
    });
    afterAll(cleanup);

    it("shows a button to open menu", () => {
      const button = screen.getByRole("button", { name: "Open profile menu" });
      expect(button).toBeEnabled();
    });

    it("shows popup information on the button to support assistive technology", () => {
      const button = screen.getByRole("button", { name: "Open profile menu" });

      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it("shows that menu is not expanded on the button to support assistive technology", () => {
      const button = screen.getByRole("button", { name: "Open profile menu" });

      expect(button).toHaveAttribute("aria-expanded", "false");
    });
  });

  describe("renders all necessary elements when dropdown is open", () => {
    beforeAll(async () => {
      mockAuthUser.mockReturnValue(testUser);
      customRender(<ProfileDropdown />, { memoryRouter: true });
      const button = screen.getByRole("button", { name: "Open profile menu" });
      await user.click(button);
    });

    afterAll(cleanup);

    it("shows the user name", () => {
      const userName = screen.getByText(testUser.username);

      expect(userName).toBeVisible();
    });

    it("shows the team name", () => {
      const teamName = screen.getByText(testUser.teamname);

      expect(teamName).toBeVisible();
    });

    it("shows all necessary menu items", () => {
      const menuItems = screen.getAllByRole("menuitem");

      expect(menuItems).toHaveLength(menuItems.length);
    });

    menuItems.forEach((item) => {
      it(`shows a menu item for ${item.name}`, () => {
        const menuItem = screen.getByRole("menuitem", { name: item.name });

        expect(menuItem).toBeVisible();
      });
    });
  });

  describe("handles user choosing items from the menu", () => {
    beforeEach(() => {
      mockAuthUser.mockReturnValue(testUser);
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      customRender(<ProfileDropdown />, { memoryRouter: true });
    });

    afterEach(() => {
      jest.restoreAllMocks();
      cleanup();
    });

    menuItems.forEach((item) => {
      const name = item.name;

      const path = item.path;

      it(`navigates to "${path}" when user clicks "${name}"`, async () => {
        const button = screen.getByRole("button", {
          name: "Open profile menu",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", { name: name });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith(path);
      });
    });
  });

  describe("handles user sucessfully login out", () => {
    beforeEach(() => {
      mockAuthUser.mockReturnValue(testUser);
      // calling '/logout` successfully will  resolve in us
      // receiving a 401 error so this is mocking the real behavior
      mockLogoutUser.mockRejectedValue({ status: 401 });
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      customRender(<ProfileDropdown />, { memoryRouter: true });
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("logs out user when they click log out in menu", async () => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      await user.click(button);

      const logout = screen.getByRole("menuitem", { name: "Log out" });
      await user.click(logout);

      expect(mockLogoutUser).toHaveBeenCalled();
    });

    it("shows a progress notification to user while logout is in progress", async () => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      await user.click(button);

      const logout = screen.getByRole("menuitem", { name: "Log out" });
      await user.click(logout);

      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: "You are being logged out of Klaw...",
        })
      );
    });

    it("redirects user to /login when they have logged out", async () => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      await user.click(button);

      const logout = screen.getByRole("menuitem", { name: "Log out" });
      await user.click(logout);

      expect(window.location.assign).toHaveBeenCalledWith(
        "http://localhost/login"
      );
    });
  });

  describe("handles error in logout process", () => {
    mockAuthUser.mockReturnValue(testUser);
    const testError = {
      status: 500,
      message: "bad error",
    };

    beforeEach(() => {
      jest.spyOn(console, "error").mockImplementation((error) => error);
      mockLogoutUser.mockRejectedValue(testError);
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      customRender(<ProfileDropdown />, { memoryRouter: true });
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("does not redirect user to /login", async () => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      await user.click(button);

      const logout = screen.getByRole("menuitem", { name: "Log out" });
      await user.click(logout);

      expect(window.location.assign).not.toHaveBeenCalled();
      expect(console.error).toHaveBeenCalledWith(testError);
    });

    it("shows error notification to the user", async () => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      await user.click(button);

      const logout = screen.getByRole("menuitem", { name: "Log out" });
      await user.click(logout);

      expect(mockDismiss).toHaveBeenCalledWith("logout");

      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message:
            "Something went wrong in the log out process. Please try again or contact your administrator.",
        })
      );
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });
});
