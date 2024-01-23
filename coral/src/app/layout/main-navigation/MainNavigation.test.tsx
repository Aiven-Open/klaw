import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";
import { KlawApiError } from "src/services/api";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  tabThroughBackward,
  tabThroughForward,
} from "src/services/test-utils/tabbing";

jest.mock("src/domain/team/team-api.ts");

const mockGetRequestsStatistics = jest.fn();
const mockGetRequestsWaitingForApproval = jest.fn();
jest.mock("src/domain/requests/requests-api.ts", () => ({
  getRequestsStatistics: () => mockGetRequestsStatistics(),
  getRequestsWaitingForApproval: () => mockGetRequestsWaitingForApproval(),
}));

jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => {
    return testAuthUser;
  },
}));

const isFeatureFlagActiveMock = jest.fn();
jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

const mockedUseToast = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
  // This is mocked at the test-setup level, but we need to do it again
  // Because we requireActual("@aivenio/aquarium") to mock useToast here
  // And this overrides the requireActual at test-setup level
  Icon: jest.fn((props) => {
    return <div data-testid={"ds-icon"} data-icon={props.icon.body} />;
  }),
}));

const navLinks = [
  {
    name: "Dashboard",
    linkTo: isFeatureFlagActiveMock() ? "/" : "/index",
  },
  {
    name: "Topics",
    linkTo: "/topics",
  },
  {
    name: "Connectors",
    linkTo: "/connectors",
  },
  { name: "Approve requests", linkTo: "/approvals" },
  { name: "My team's requests", linkTo: "/requests" },
  {
    name: "Activity log",
    linkTo: "/activity-log",
  },
];

const submenuItems = [
  {
    name: "Configuration overview",
    links: [
      { name: "Users", linkTo: "/configuration/users" },
      {
        name: "Teams",
        linkTo: `/configuration/teams`,
      },
      {
        name: "Environments",
        linkTo: "/configuration/environments",
      },
      {
        name: "Clusters",
        linkTo: "/configuration/clusters",
      },
    ],
  },
  {
    name: "User information",
    links: [
      {
        name: "User profile",
        linkTo: "/user/profile",
      },
      {
        name: "Change password",
        linkTo: "/user/change-password",
      },
      {
        name: "Tenant information",
        linkTo: "/user/tenant-info",
      },
    ],
  },
];

const navOrderFirstLevel = [
  { name: "Dashboard", isSubmenu: false },
  { name: "Topics", isSubmenu: false },
  { name: "Connectors", isSubmenu: false },
  { name: "Approve requests", isSubmenu: false },
  { name: "My team's requests", isSubmenu: false },
  { name: "Activity log", isSubmenu: false },
  { name: "Configuration", isSubmenu: true },
  { name: "User information", isSubmenu: true },
];

describe("MainNavigation.tsx", () => {
  const originalConsoleError = console.error;

  beforeEach(() => {
    console.error = jest.fn();

    mockGetRequestsStatistics.mockResolvedValue([]);
    mockGetRequestsWaitingForApproval.mockResolvedValue([]);
  });

  afterAll(() => {
    console.error = originalConsoleError;
  });

  describe("renders the main navigation in default state", () => {
    beforeEach(() => {
      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
    });

    afterEach(cleanup);

    it("renders the main navigation", () => {
      const nav = screen.getByRole("navigation", { name: "Main navigation" });
      expect(nav).toBeVisible();
    });

    it("renders the user's current team", async () => {
      const teamLabel = screen.getByText("Team");
      const teamName = screen.getByText(testAuthUser.teamname);
      expect(teamLabel).toBeVisible();
      expect(teamName).toBeVisible();
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
      // every nav link and submenu link has one icon
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
        aquariumContext: true,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

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

          submenu.links.forEach(({ name, linkTo }) => {
            const link = within(list).getByRole("link", { name });
            expect(link).toBeVisible();
            expect(link).toHaveAttribute("href", linkTo);
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
          aquariumContext: true,
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
        const lastElement = navOrderFirstLevel[navOrderFirstLevel.length - 1];
        customRender(<MainNavigation />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        });
        const lastNavItem = screen.getByRole(
          lastElement.isSubmenu ? "button" : "link",
          {
            name: new RegExp(lastElement.name, "i"),
          }
        );
        lastNavItem.focus();
      });

      afterEach(cleanup);

      const navOrderReversed = [...navOrderFirstLevel].reverse();
      navOrderReversed.forEach((link, index) => {
        const name = link.name;
        const element = link.isSubmenu ? "button" : "link";
        const numbersOfTabs = index;
        it(`sets focus to ${element} ${link.name} when user shift+tabs ${numbersOfTabs} times`, async () => {
          const link = screen.getByRole(element, {
            name: new RegExp(name, "i"),
          });
          index > 0 && expect(link).not.toHaveFocus();
          await tabThroughBackward(numbersOfTabs);

          expect(link).toHaveFocus();
        });
      });
    });

    describe("renders links to dashboard behind feature flag", () => {
      afterEach(() => {
        cleanup();
        jest.resetAllMocks();
      });

      it("renders a link to the old UI when feature flag is false", async () => {
        isFeatureFlagActiveMock.mockReturnValue(false);
        customRender(<MainNavigation />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        });

        const link = screen.getByRole("link", { name: "Dashboard" });
        expect(link).toBeVisible();
        expect(link).toHaveAttribute("href", "/index");
      });

      it("renders a link to the dashboard page when feature flag is true", async () => {
        isFeatureFlagActiveMock.mockReturnValue(true);
        customRender(<MainNavigation />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        });

        const link = screen.getByRole("link", { name: "Dashboard" });
        expect(link).toBeVisible();
        expect(link).toHaveAttribute("href", "/");
      });
    });
  });

  describe("shows an toast error notification when fetching pending requests fails", () => {
    const testError: KlawApiError = {
      message: "Oh no, this did not work",
      success: false,
    };

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("calls useToast with correct error message", async () => {
      mockGetRequestsWaitingForApproval.mockRejectedValue(testError);

      customRender(<MainNavigation />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: `Could not fetch pending requests: ${testError.message}`,
          })
        )
      );
    });
  });
});
