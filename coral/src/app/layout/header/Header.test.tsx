import Header from "src/app/layout/header/Header";
import { cleanup, screen, render, within } from "@testing-library/react";
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

const quickLinksNavItems = [
  { name: "Go to approval requests", linkTo: "/execTopics" },
  {
    name: "Go to Klaw documentation page",
    linkTo: "https://www.klaw-project.io/docs",
  },
  { name: "Go to your profile", linkTo: "/myProfile" },
];

describe("Header.tsx", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<Header />);
    });

    afterAll(cleanup);

    it("renders a header element", () => {
      const heading = screen.getByRole("banner");

      expect(heading).toBeVisible();
    });

    it("renders a link to the Klaw hompeage", () => {
      const link = screen.getByRole("link", { name: "Klaw homepage" });

      expect(link).toBeVisible();
    });

    it("renders the Klaw logo inside link hidden from assistive technology", () => {
      const link = screen.getByRole("link", { name: "Klaw homepage" });
      const logo = link.querySelector("img");

      expect(logo).toHaveAttribute("src");
      expect(logo).toHaveAttribute("aria-hidden", "true");
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
    const allHeaderLinks = [
      "Klaw homepage",
      ...quickLinksNavItems.map((link) => link.name),
    ];

    describe("user can navigate through links", () => {
      beforeEach(() => {
        render(<Header />);
        const heading = screen.getByRole("banner");
        heading.focus();
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
        render(<Header />);
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
