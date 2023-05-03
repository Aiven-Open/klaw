import { cleanup, screen, within } from "@testing-library/react";
import Header from "src/app/layout/header/Header";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";

const quickLinksNavItems = [
  { name: "Go to approve requests", linkTo: "/approvals/topics" },
  {
    name: "Go to Klaw documentation page",
    linkTo: "https://www.klaw-project.io/docs",
  },
  { name: "Go to your profile", linkTo: "/myProfile" },
];

describe("Header.tsx", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      customRender(<Header />, { memoryRouter: true });
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
      it(`renders a link to ${item.name}`, () => {
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
    const allHeaderElements = [
      "Klaw homepage",
      "Request a new",
      ...quickLinksNavItems.map((link) => link.name),
    ];

    describe("user can navigate through elements", () => {
      beforeEach(() => {
        customRender(<Header />, { memoryRouter: true });
        const heading = screen.getByRole("banner");
        heading.focus();
      });

      afterEach(cleanup);

      allHeaderElements.forEach((headerElement, index) => {
        const numbersOfTabs = index + 1;
        it(`sets focus on ${headerElement} when user tabs ${numbersOfTabs} times`, async () => {
          const element =
            headerElement !== "Request a new"
              ? screen.getByRole("link", { name: headerElement })
              : screen.getByRole("button", { name: headerElement });

          expect(element).not.toHaveFocus();

          await tabThroughForward(numbersOfTabs);

          expect(element).toHaveFocus();
        });
      });
    });

    describe("user can navigate backward through links", () => {
      beforeEach(() => {
        customRender(<Header />, { memoryRouter: true });
        const lastElement = allHeaderElements[allHeaderElements.length - 1];
        const lastNavItem = screen.getByRole("link", {
          name: lastElement,
        });
        lastNavItem.focus();
      });

      afterEach(cleanup);

      const allHeaderElementsReversed = [...allHeaderElements].reverse();
      allHeaderElementsReversed.forEach((headerElement, index) => {
        const numbersOfTabs = index;

        it(`sets focus on ${headerElement} when user shift+tabs ${numbersOfTabs} times`, async () => {
          const element =
            headerElement !== "Request a new"
              ? screen.getByRole("link", { name: headerElement })
              : screen.getByRole("button", { name: headerElement });
          index > 0 && expect(element).not.toHaveFocus();

          await tabThroughBackward(numbersOfTabs);

          expect(element).toHaveFocus();
        });
      });
    });
  });
});
