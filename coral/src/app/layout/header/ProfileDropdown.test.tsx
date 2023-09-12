import { cleanup, screen, render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProfileDropdown } from "src/app/layout/header/ProfileDropdown";

const menuItems = [
  { path: "/myProfile", name: "My profile" },
  { path: "/tenantInfo", name: "My tenant info" },
  { path: "/changePwd", name: "Change password" },
];

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
});
