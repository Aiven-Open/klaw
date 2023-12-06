import { cleanup, screen, within } from "@testing-library/react";
import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import { Routes } from "src/app/router_utils";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";
import * as hook from "src/app/hooks/usePendingRequests";

const mockToast = jest.fn();
const mockDismiss = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToastContext: () => [mockToast, mockDismiss],
}));

const quickLinksNavItems = [
  { name: "Go to approve requests", linkTo: Routes.APPROVALS },
  {
    name: "Go to Klaw documentation page",
    linkTo: "https://www.klaw-project.io/docs",
  },
];

const mockedNoPendingRequests = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 0,
  CONNECTOR: 0,
  USER: 0,
  OPERATIONAL: 0,
  TOTAL: 0,
};

const mockedPendingRequests = {
  TOPIC: 1,
  ACL: 0,
  SCHEMA: 3,
  CONNECTOR: 2,
  USER: 2,
  OPERATIONAL: 0,
  TOTAL: 8,
};

describe("HeaderNavigation.tsx", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      jest
        .spyOn(hook, "usePendingRequests")
        .mockImplementation(() => mockedNoPendingRequests);

      customRender(<HeaderNavigation />, { memoryRouter: true });
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders a navigation element with quick links", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });

      expect(nav).toBeVisible();
    });

    quickLinksNavItems.forEach((item) => {
      it(`renders a link to ${item.name}`, () => {
        const nav = screen.getByRole("navigation", { name: "Quick links" });
        const link = within(nav).getByRole("link", { name: item.name });

        expect(link).toBeEnabled();
        expect(link).toHaveAttribute("href", item.linkTo);
      });
    });

    it(`renders a button for the profile dropdown`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const button = within(nav).getByRole("button", {
        name: "Open profile menu",
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it("renders all links in the header menu", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const links = within(nav).getAllByRole("link");

      expect(links).toHaveLength(quickLinksNavItems.length);
    });
  });

  describe("enables user to navigate with keyboard only", () => {
    const allHeaderElements = [
      "Request a new",
      ...quickLinksNavItems.map((link) => link.name),
      "Open profile menu",
    ];

    describe("user can navigate through elements", () => {
      beforeEach(() => {
        customRender(<HeaderNavigation />, { memoryRouter: true });
        const navigation = screen.getByRole("navigation");
        navigation.focus();
      });

      afterEach(cleanup);

      allHeaderElements.forEach((headerElement, index) => {
        const numbersOfTabs = index + 1;
        it(`sets focus on ${headerElement} when user tabs ${numbersOfTabs} times`, async () => {
          const element =
            headerElement !== "Request a new" &&
            headerElement !== "Open profile menu"
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
        customRender(<HeaderNavigation />, { memoryRouter: true });
        const lastElement = allHeaderElements[allHeaderElements.length - 1];
        const lastNavItem = screen.getByRole("button", {
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
            headerElement !== "Request a new" &&
            headerElement !== "Open profile menu"
              ? screen.getByRole("link", { name: headerElement })
              : screen.getByRole("button", { name: headerElement });
          index > 0 && expect(element).not.toHaveFocus();

          await tabThroughBackward(numbersOfTabs);

          expect(element).toHaveFocus();
        });
      });
    });
  });

  describe("shows notification on Approve request link when there are pending requests", () => {
    beforeAll(() => {
      jest
        .spyOn(hook, "usePendingRequests")
        .mockImplementation(() => mockedPendingRequests);
      customRender(<HeaderNavigation />, { memoryRouter: true });
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders correct link text", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const approvalsLink = within(nav).getAllByRole("link")[0];

      expect(approvalsLink).toHaveAttribute("href", "/approvals");

      const linkText = within(approvalsLink).getByText(
        "Go to approve 8 pending requests"
      );

      expect(linkText).toBeVisible();
    });
  });
});
