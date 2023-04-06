import * as ReactQuery from "@tanstack/react-query";
import { cleanup, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";

const useQuerySpy = jest.spyOn(ReactQuery, "useQuery");

const navLinks = [
  {
    name: "Dashboard",
    linkTo: "/index",
  },
  {
    name: "Topics",
    linkTo: "/topics",
  },
  {
    name: "Kafka Connectors",
    linkTo: "/connectors",
  },
  { name: "Approve requests", linkTo: "/approvals" },
  { name: "My team's requests", linkTo: "/requests" },
  { name: "Audit log", linkTo: "/activityLog" },
  { name: "Settings", linkTo: "/serverConfig" },
];

const submenuItems = [
  { name: "Users and teams", links: ["Users", "Teams", "User requests"] },
];

const navOrderFirstLevel = [
  { name: "Dashboard", isSubmenu: false },
  { name: "Topics", isSubmenu: false },
  { name: "Kafka Connectors", isSubmenu: false },
  { name: "Users and teams", isSubmenu: true },
  { name: "Approve requests", isSubmenu: false },
  { name: "My team's requests", isSubmenu: false },
  { name: "Audit log", isSubmenu: false },
  { name: "Settings", isSubmenu: false },
];

describe("MainNavigation.tsx", () => {
  describe("renders the main navigation in default state", () => {
    beforeEach(() => {
      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterEach(cleanup);

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

    it(`renders icons for all nav links that are hidden from assistive technology`, () => {
      // every navlink and submenu link has one icon
      // every submenu link has an icon to indicate opened/closed
      const iconAmount = navLinks.length + submenuItems.length * 2;
      const nav = screen.getByRole("navigation", {
        name: "Main navigation",
      });

      const icons = within(nav).getAllByTestId("ds-icon");
      expect(icons).toHaveLength(iconAmount);
    });
  });

  describe("user can open submenus and see more links", () => {
    beforeEach(() => {
      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
      });
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
  });

  describe("enables user to navigate with keyboard only", () => {
    describe("user can navigate through first level navigation", () => {
      beforeEach(() => {
        customRender(<MainNavigation />, {
          memoryRouter: true,
          queryClient: true,
        });
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
        customRender(<MainNavigation />, {
          memoryRouter: true,
          queryClient: true,
        });
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

  describe("user can see their current team", () => {
    afterEach(() => {
      cleanup();
      useQuerySpy.mockClear();
    });

    it("renders loading state", async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: undefined, isLoading: true });
      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
      });

      const teamLabel = screen.getByText("Team");
      const teamName = screen.getByText("Fetching team...");
      expect(teamLabel).toBeVisible();
      expect(teamName).toBeVisible();
    });

    it("renders the user's current team", async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: "Team-name", isLoading: false });
      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
      });

      const teamLabel = screen.getByText("Team");
      const teamName = screen.getByText("Team-name");
      expect(teamLabel).toBeVisible();
      expect(teamName).toBeVisible();
    });
  });
});
