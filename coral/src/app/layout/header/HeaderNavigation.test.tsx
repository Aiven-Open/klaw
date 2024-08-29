import { cleanup, screen, within } from "@testing-library/react";
import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";
import * as hook from "src/app/hooks/usePendingRequests";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";
import { UseAuthContext } from "src/app/context-provider/AuthProvider";

const mockToast = jest.fn();
const mockDismiss = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToastContext: () => [mockToast, mockDismiss],
}));

const mockedNoPendingRequests = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 0,
  CONNECTOR: 0,
  USER: 0,
  OPERATIONAL: 0,
  TOTAL_NOTIFICATIONS: 0,
};

const mockedPendingRequests = {
  TOPIC: 1,
  ACL: 0,
  SCHEMA: 3,
  CONNECTOR: 2,
  USER: 2,
  OPERATIONAL: 0,
  TOTAL_NOTIFICATIONS: 6,
};

let mockAuthUserContext: UseAuthContext = {
  ...testAuthUser,
  isSuperAdminUser: false,
};
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUserContext,
}));

describe("HeaderNavigation.tsx", () => {
  const requestANewButton = "Request a new";
  const defaultPendingRequestsButton = "No pending requests";
  const profileButton = "Open profile menu";
  const documentationPageLink = "Go to Klaw documentation page";

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

    it("renders a button to request a new entity", () => {
      const button = screen.getByRole("button", {
        name: requestANewButton,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it("renders a navigation element with quick links", () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });

      expect(nav).toBeVisible();
    });

    it(`renders a button to show open requests`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const button = within(nav).getByRole("button", {
        name: defaultPendingRequestsButton,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it(`renders a button for the profile dropdown`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const button = within(nav).getByRole("button", {
        name: profileButton,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it(`renders a link to Klaw documentation page`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const link = within(nav).getByRole("link", {
        name: documentationPageLink,
      });

      expect(link).toBeEnabled();
      expect(link).toHaveAttribute("href", "https://www.klaw-project.io/docs");
    });
  });

  describe("removes specific elements if user is superadmin", () => {
    beforeAll(() => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: true };
      jest
        .spyOn(hook, "usePendingRequests")
        .mockImplementation(() => mockedNoPendingRequests);

      customRender(<HeaderNavigation />, { memoryRouter: true });
    });

    afterAll(() => {
      mockAuthUserContext = { ...testAuthUser, isSuperAdminUser: false };
      cleanup();
      jest.clearAllMocks();
    });

    it("does not show a button to request a new entity", () => {
      const button = screen.queryByText(requestANewButton);

      expect(button).not.toBeInTheDocument();
    });

    it(`does not show a button to show open requests`, () => {
      const button = screen.queryByText(defaultPendingRequestsButton);

      expect(button).not.toBeInTheDocument();
    });

    it(`renders a button for the profile dropdown`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const button = within(nav).getByRole("button", {
        name: profileButton,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it(`renders a link to Klaw documentation page`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const link = within(nav).getByRole("link", {
        name: documentationPageLink,
      });

      expect(link).toBeEnabled();
      expect(link).toHaveAttribute("href", "https://www.klaw-project.io/docs");
    });
  });

  describe("enables user to navigate with keyboard only", () => {
    const allHeaderElements = [
      "Request a new",
      "No pending requests",
      "Open profile menu",
      "Go to Klaw documentation page",
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
            headerElement === "Go to Klaw documentation page"
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
            headerElement === "Go to Klaw documentation page"
              ? screen.getByRole("link", { name: headerElement })
              : screen.getByRole("button", { name: headerElement });
          index > 0 && expect(element).not.toHaveFocus();

          await tabThroughBackward(numbersOfTabs);

          expect(element).toHaveFocus();
        });
      });
    });
  });

  describe("shows notification on Approve request button when there are pending requests", () => {
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

    it("renders correct button", async () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const button = await within(nav).findByRole("button", {
        name: "See 6 pending requests",
      });

      expect(button).toBeVisible();
    });
  });
});
