import { cleanup, screen, within } from "@testing-library/react";
import LayoutWithoutNav from "src/app/layout/LayoutWithoutNav";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabThroughForward } from "src/services/test-utils/tabbing";

const isFeatureFlagActiveMock = jest.fn();

jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => jest.fn(),
}));

describe("LayoutWithoutNav.tsx", () => {
  isFeatureFlagActiveMock.mockReturnValue(true);

  describe("renders the layout component with all needed elements", () => {
    beforeAll(() => {
      customRender(<LayoutWithoutNav />, {
        browserRouter: true,
        queryClient: true,
      });
    });

    afterAll(cleanup);

    it("renders a button to skip to main content for assistive technology", () => {
      const skipLink = screen.getByRole("button", {
        name: "Skip to main content",
      });

      expect(skipLink).toBeEnabled();
    });

    it("renders the header", () => {
      const header = screen.getByRole("banner");
      expect(header).toBeVisible();
    });

    it("does not render the main navigation", () => {
      const nav = screen.queryByRole("navigation", { name: "Main navigation" });
      expect(nav).not.toBeInTheDocument();
    });
  });

  describe("enables user to navigate all navigation element with keyboard", () => {
    beforeEach(() => {
      customRender(<LayoutWithoutNav />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterEach(cleanup);

    it("sets focus on the skip link when user tabs the first time", async () => {
      const skipLink = screen.getByRole("button", {
        name: "Skip to main content",
      });

      expect(skipLink).not.toHaveFocus();
      await tabThroughForward(1);

      expect(skipLink).toHaveFocus();
    });

    it("sets focus on the link to the Klaw homepage if user tabs 2 times", async () => {
      const homeLink = screen.getByRole("link", { name: "Klaw homepage" });
      expect(homeLink).not.toHaveFocus();

      await tabThroughForward(2);

      expect(homeLink).toHaveFocus();
    });

    it("sets focus on the Create a new entity dropdown of the quick link navigation if user tabs 3 times", async () => {
      const dropdown = screen.getByRole("button", {
        name: "Request a new",
      });

      expect(dropdown).not.toHaveFocus();
      await tabThroughForward(3);

      expect(dropdown).toHaveFocus();
    });

    it("sets focus on the first element of the quick link navigation if user tabs 4 times", async () => {
      const quickLinks = screen.getByRole("navigation", {
        name: "Quick links",
      });
      const link = within(quickLinks).getAllByRole("link")[0];

      expect(link).not.toHaveFocus();
      await tabThroughForward(4);

      expect(link).toHaveFocus();
    });
  });
});
