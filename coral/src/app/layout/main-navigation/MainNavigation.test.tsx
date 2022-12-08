import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import { cleanup, screen, render, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// mock out svgs to avoid clutter
jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),

    Icon: () => {
      return <div data-testid={"ds-icon"}></div>;
    },
  };
});

const navLinks = [
  {
    name: "Overviews",
    linkTo: "/index",
  },
  { name: "Audit Log", linkTo: "/activityLog" },
  { name: "Settings", linkTo: "/serverConfig" },
];

const submenuItems = [
  { name: "Kafka Connector", links: ["All Connectors", "Connectors Requests"] },
  { name: "Users and Teams", links: ["Users", "Teams", "User Requests"] },
];

// Topics is temp opened in default state and required special testing
// until routing takes care of that. Tried to implement the tests in
// a way that it updating tests is uncomplicated
const submenuItemTopics = [
  {
    name: "Topics",
    links: ["All Topics", "Approval Requests", "My Team's Requests"],
  },
];

describe("SidebarNavigation.tsx", () => {
  describe("renders the main navigation in default state", () => {
    beforeAll(() => {
      render(<MainNavigation />);
    });

    afterAll(cleanup);

    it("renders the main navigation", () => {
      const nav = screen.getByRole("navigation", { name: "Main navigation" });
      expect(nav).toBeVisible();
    });

    navLinks.forEach((link) => {
      it(`renders a link for ${link.name}`, () => {
        const nav = screen.getByRole("navigation", {
          name: "Main navigation",
        });

        const navLink = within(nav).getByRole("link", { name: link.name });
        expect(navLink).toBeVisible();
        expect(navLink).toHaveAttribute("href", link.linkTo);
      });
    });

    it(`renders all navigation items`, () => {
      const navLinks = screen.getAllByRole("link");

      expect(navLinks).toHaveLength(navLinks.length);
    });

    submenuItems.forEach((submenu) => {
      it(`renders a button to open submenu for ${submenu.name}`, () => {
        const nav = screen.getByRole("navigation", {
          name: "Main navigation",
        });

        const button = within(nav).getByRole("button", {
          name: `${submenu.name} submenu, closed. Click to open.`,
        });

        expect(button).toBeEnabled();
      });
    });

    it(`renders all submenu buttons`, () => {
      const submenuItems = screen.getAllByRole("button");

      expect(submenuItems).toHaveLength(submenuItems.length);
    });

    // temp for special handling of "Topics"
    submenuItemTopics.forEach((submenu) => {
      it(`renders a button to close submenu for ${submenu.name}`, () => {
        const nav = screen.getByRole("navigation", {
          name: "Main navigation",
        });

        const button = within(nav).getByRole("button", {
          name: `${submenu.name} submenu, open. Click to close.`,
        });

        expect(button).toBeEnabled();
      });

      it(`renders a submenu list for ${submenu.name}`, () => {
        const list = screen.getByRole("list", {
          name: `${submenu.name} submenu`,
        });
        expect(list).toBeVisible();
      });

      submenu.links.forEach((link) => {
        it(`renders the submenu links ${link}`, () => {
          const menu = screen.getByRole("list", {
            name: `${submenu.name} submenu`,
          });
          const submenuLink = within(menu).getByRole("link", { name: link });
          expect(submenuLink).toBeVisible();
        });
      });
    });

    it(`renders icons for all nav links that are hidden from assistive technology`, () => {
      // every navlink and submenu link has one icon
      // every submenu link has an icon to indicate opened/closed
      const iconAmount =
        navLinks.length +
        submenuItems.length * 2 +
        submenuItemTopics.length * 2;
      const nav = screen.getByRole("navigation", {
        name: "Main navigation",
      });

      const icons = within(nav).getAllByTestId("ds-icon");
      expect(icons).toHaveLength(iconAmount);
    });
  });

  describe("user can open submenus and see more links", () => {
    beforeEach(() => {
      render(<MainNavigation />);
    });

    afterEach(cleanup);

    submenuItems.forEach((submenu) => {
      describe(`shows all submenu items for ${submenu.name} when user opens menu`, () => {
        it(`does not show a list with more links for submenu  ${submenu.name}`, () => {
          const list = screen.queryByRole("list", {
            name: `${submenu.name} submenu`,
            hidden: true,
          });
          expect(list).not.toBeInTheDocument();
        });

        it(`opens the ${submenu.name} and shows a list with more links`, async () => {
          const button = screen.getByRole("button", {
            name: new RegExp(submenu.name, "i"),
          });

          await userEvent.click(button);

          const list = screen.queryByRole("list", {
            name: `${submenu.name} submenu`,
          });
          expect(list).toBeVisible();
        });

        it(`shows all links for submenu ${submenu.name} after user opens it`, async () => {
          const button = screen.getByRole("button", {
            name: new RegExp(submenu.name, "i"),
          });
          await userEvent.click(button);
          const list = screen.getByRole("list", {
            name: `${submenu.name} submenu`,
          });
          submenu.links.forEach((linkText) => {
            const link = within(list).getByRole("link", { name: linkText });
            expect(link).toBeVisible();
          });
        });
      });
    });

    submenuItemTopics.forEach((submenu) => {
      describe(`hides all links for ${submenu.name} if user closes it`, () => {
        it(`does show a list with more links for submenu  ${submenu.name}`, () => {
          const list = screen.getByRole("list", {
            name: `${submenu.name} submenu`,
          });
          expect(list).toBeVisible();
        });

        it(`closes the ${submenu.name} and hides the links if user closes it`, async () => {
          const button = screen.getByRole("button", {
            name: new RegExp(submenu.name, "i"),
          });

          await userEvent.click(button);

          const list = screen.queryByRole("list", {
            name: `${submenu.name} submenu`,
            hidden: true,
          });
          expect(list).not.toBeInTheDocument();
        });

        it(`hides all links for submenu ${submenu.name} after user closes it`, async () => {
          const button = screen.getByRole("button", {
            name: new RegExp(submenu.name, "i"),
          });
          await userEvent.click(button);

          submenu.links.forEach((linkText) => {
            const link = screen.queryByRole("link", {
              name: linkText,
              hidden: true,
            });
            expect(link).not.toBeInTheDocument();
          });
        });
      });
    });
  });
});
