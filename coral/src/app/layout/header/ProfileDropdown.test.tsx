import { cleanup, screen, render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProfileDropdown } from "src/app/layout/header/ProfileDropdown";
import { logoutUser } from "src/domain/auth-user";

const menuItems = [
  { path: "/myProfile", name: "My profile" },
  { path: "/tenantInfo", name: "My tenant info" },
  { path: "/changePwd", name: "Change password" },
];

jest.mock("src/domain/auth-user/auth-user-api");

const mockLogoutUser = logoutUser as jest.MockedFunction<typeof logoutUser>;

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));
describe("ProfileDropdown", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements when dropdown is closed", () => {
    beforeAll(() => {
      render(<ProfileDropdown />);
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
      render(<ProfileDropdown />);
      const button = screen.getByRole("button", { name: "Open profile menu" });
      await user.click(button);
    });

    afterAll(cleanup);

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
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      render(<ProfileDropdown />);
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

        // in tests it will start with http://localhost/ since that is the window.origin
        expect(window.location.assign).toHaveBeenCalledWith(
          `http://localhost${path}`
        );
      });
    });
  });

  describe("handles user sucessfully login out", () => {
    beforeEach(() => {
      // calling '/logout` successfully will  resolve in us
      // receiving a 401 error so this is mocking the real behavior
      mockLogoutUser.mockRejectedValue({ status: 401 });
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      render(<ProfileDropdown />);
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
    const testError = {
      status: 500,
      message: "bad error",
    };
    const originalConsoleError = console.error;

    beforeEach(() => {
      mockLogoutUser.mockRejectedValue(testError);

      console.error = jest.fn();
      Object.defineProperty(window, "location", {
        value: {
          assign: jest.fn(),
        },
        writable: true,
      });
      render(<ProfileDropdown />);
    });

    afterEach(() => {
      console.error = originalConsoleError;
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

      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message:
            "Something went wrong in the log out process. Please try again or contact your administrator.",
        })
      );
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });
});
