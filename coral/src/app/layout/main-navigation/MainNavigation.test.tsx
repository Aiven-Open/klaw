import { cleanup, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";

// mock out svgs to avoid clutter
jest.mock("@aivenio/aquarium", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/aquarium"),

    Icon: () => {
      return <div data-testid={"ds-icon"}></div>;
    },
  };
});

const navLinks = [
  {
    name: "Dashboard",
    linkTo: "/index",
  },
  { name: "Approval Requests", linkTo: "/approvals" },
  { name: "Audit Log", linkTo: "/activityLog" },
  { name: "Settings", linkTo: "/serverConfig" },
];

const submenuItems = [
  { name: "Kafka Connectors", links: ["All Connectors", "Connector Requests"] },
  { name: "Users and Teams", links: ["Users", "Teams", "User Requests"] },
];

// Topics is temp opened in default state and required special testing
// until routing takes care of that. Tried to implement the tests in
// a way that it updating tests is uncomplicated
const submenuItemTopics = [
  {
    name: "Topics",
    links: ["All Topics", "My Team's Requests"],
  },
];

// submenus from "Topics" can be removed
// for this test case when "Topics"
// is no longer open per default
// keyboard a11y is tested in submenu component
const navOrderFirstLevel = [
  { name: "Dashboard", isSubmenu: false },
  { name: "Topics", isSubmenu: true },
  { name: "All Topics", isSubmenu: false },
  { name: "My Team's Requests", isSubmenu: false },
  { name: "Kafka Connectors", isSubmenu: true },
  { name: "Users and Teams", isSubmenu: true },
  { name: "Approval Requests", isSubmenu: false },
  { name: "Audit Log", isSubmenu: false },
  { name: "Settings", isSubmenu: false },
];

describe("MainNavigation.tsx", () => {
  beforeAll(() => {
    process.env.FEATURE_FLAG_APPROVALS = "true";
  });
  afterAll(() => {
    process.env.FEATURE_FLAG_APPROVALS = "false";
  });

  describe("renders the main navigation in default state", () => {
    beforeAll(() => {
      customRender(<MainNavigation />, { memoryRouter: true });
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
      customRender(<MainNavigation />, { memoryRouter: true });
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

  describe("enables user to navigate with keyboard only", () => {
    describe("user can navigate through first level navigation", () => {
      beforeEach(() => {
        customRender(<MainNavigation />, { memoryRouter: true });
        const nav = screen.getByRole("navigation", { name: "Main navigation" });
        nav.focus();
      });

      afterEach(cleanup);

      navOrderFirstLevel.forEach((link, index) => {
        const name = link.name;
        const element = link.isSubmenu ? "button" : "link";
        const numbersOfTabs = index + 1;

        it(`sets focus to link ${link.name} when user tabs ${numbersOfTabs} times`, async () => {
          const link = screen.getByRole(element, {
            name: new RegExp(name, "i"),
          });
          expect(link).not.toHaveFocus();
          await tabThroughForward(numbersOfTabs);

          expect(link).toHaveFocus();
        });
      });
    });

    describe("user can navigate backward through first level navigation", () => {
      beforeEach(() => {
        const lastElement =
          navOrderFirstLevel[navOrderFirstLevel.length - 1].name;
        customRender(<MainNavigation />, { memoryRouter: true });
        const lastNavItem = screen.getByRole("link", {
          name: lastElement,
        });
        lastNavItem.focus();
      });

      afterEach(cleanup);

      const navOrderReversed = [...navOrderFirstLevel].reverse();
      navOrderReversed.forEach((link, index) => {
        const name = link.name;
        const element = link.isSubmenu ? "button" : "link";
        const numbersOfTabs = index;

        it(`sets focus to link ${link.name} when user shift+tabs ${numbersOfTabs} times`, async () => {
          const link = screen.getByRole(element, {
            name: new RegExp(name, "i"),
          });
          index > 0 && expect(link).not.toHaveFocus();
          await tabThroughBackward(numbersOfTabs);

          expect(link).toHaveFocus();
        });
      });
    });
  });

  describe("renders link dependent on feature flag FEATURE_FLAG_APPROVALS", () => {
    beforeAll(() => {
      process.env.FEATURE_FLAG_APPROVALS = "false";
      customRender(<MainNavigation />, { memoryRouter: true });
    });

    afterAll(cleanup);

    it("does show link to angular app when feature flag is false", () => {
      const nav = screen.getByRole("navigation", {
        name: "Main navigation",
      });

      const navLink = within(nav).getByRole("link", {
        name: "Approval Requests",
      });

      expect(navLink).toHaveAttribute("href", "/execTopics");
    });
  });
});
