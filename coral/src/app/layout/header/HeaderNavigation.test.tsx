import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import { cleanup, screen, render, within } from "@testing-library/react";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";

const quickLinksNavItems = [
  { name: "Go to approval requests", linkTo: "/execTopics" },
  {
    name: "Go to Klaw documentation page",
    linkTo: "https://www.klaw-project.io/docs",
  },
  { name: "Go to your profile", linkTo: "/myProfile" },
];

describe("HeaderNavigation.tsx", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<HeaderNavigation />);
    });

    afterAll(cleanup);

    it("renders a navigation element with quick links", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });

      expect(nav).toBeVisible();
    });

    quickLinksNavItems.forEach((item) => {
      it(`renders a link to ${item}`, () => {
        const nav = screen.getByRole("navigation", { name: "Quick links" });
        const link = within(nav).getByRole("link", { name: item.name });

        expect(link).toBeEnabled();
        expect(link).toHaveAttribute("href", item.linkTo);
      });
    });

    it("renders all links in the header menu", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const links = within(nav).getAllByRole("link");

      expect(links).toHaveLength(quickLinksNavItems.length);
    });
  });

  describe("enables user to navigate with keyboard only", () => {
    const allHeaderLinks = quickLinksNavItems.map((link) => link.name);

    describe("user can navigate through links", () => {
      beforeEach(() => {
        render(<HeaderNavigation />);
        const navigation = screen.getByRole("navigation");
        navigation.focus();
      });

      afterEach(cleanup);

      allHeaderLinks.forEach((headerLink, index) => {
        const numbersOfTabs = index + 1;
        it(`sets focus on ${headerLink} when user tabs ${numbersOfTabs} times`, async () => {
          const link = screen.getByRole("link", { name: headerLink });
          expect(link).not.toHaveFocus();

          await tabThroughForward(numbersOfTabs);

          expect(link).toHaveFocus();
        });
      });
    });

    describe("user can navigate backward through links", () => {
      beforeEach(() => {
        render(<HeaderNavigation />);
        const lastElement = allHeaderLinks[allHeaderLinks.length - 1];
        const lastNavItem = screen.getByRole("link", {
          name: lastElement,
        });
        lastNavItem.focus();
      });

      afterEach(cleanup);

      const allHeaderLinksReversed = [...allHeaderLinks].reverse();
      allHeaderLinksReversed.forEach((headerLink, index) => {
        const numbersOfTabs = index;

        it(`sets focus on ${headerLink} when user shift+tabs ${numbersOfTabs} times`, async () => {
          const link = screen.getByRole("link", { name: headerLink });
          index > 0 && expect(link).not.toHaveFocus();

          await tabThroughBackward(numbersOfTabs);

          expect(link).toHaveFocus();
        });
      });
    });
  });
});
