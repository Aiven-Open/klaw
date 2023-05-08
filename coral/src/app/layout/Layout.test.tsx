import Layout from "src/app/layout/Layout";
import { cleanup, screen, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabThroughForward } from "src/services/test-utils/tabbing";

const testChildren = <div data-testid={"test-children"}></div>;

describe("Layout.tsx", () => {
  describe("renders the layout component with all needed elements", () => {
    beforeAll(() => {
      customRender(<Layout>{testChildren}</Layout>, {
        memoryRouter: true,
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

    it("renders the main navigation", () => {
      const nav = screen.getByRole("navigation", { name: "Main navigation" });
      expect(nav).toBeVisible();
    });

    it("renders its children in the main section", () => {
      const main = screen.getByRole("main");
      const child = within(main).getByTestId("test-children");

      expect(main).toBeVisible();
      expect(child).toBeVisible();
    });
  });

  describe("enables user to navigate all navigation element with keyboard", () => {
    beforeEach(() => {
      customRender(<Layout>{testChildren}</Layout>, {
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

    it("sets focus on the first element of the quick link navigation if user tabs 3 times", async () => {
      const quickLinks = screen.getByRole("navigation", {
        name: "Quick links",
      });
      const link = within(quickLinks).getAllByRole("link")[0];

      expect(link).not.toHaveFocus();
      await tabThroughForward(3);

      expect(link).toHaveFocus();
    });
  });
});
