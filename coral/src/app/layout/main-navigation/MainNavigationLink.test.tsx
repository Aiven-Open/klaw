import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import { cleanup, screen, render } from "@testing-library/react";
import data from "@aivenio/design-system/dist/src/icons/console";

// mock out svgs to avoid clutter
jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),

    Icon: () => {
      return <div data-testid={"ds-icon"}></div>;
    },
  };
});

describe("MainNavigationLink.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "fake-icon" as unknown as typeof data;

  describe("renders a default link with required props", () => {
    beforeAll(() => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          href={"/topics"}
          linkText={"Topics"}
        />
      );
    });

    afterAll(cleanup);

    it(`renders a link with text dependent on a given property`, () => {
      const navLink = screen.getByRole("link", { name: "Topics" });

      expect(navLink).toBeVisible();
    });

    it(`renders a href for that link dependent on a given  property`, () => {
      const navLink = screen.getByRole("link", { name: "Topics" });
      expect(navLink).toHaveAttribute("href", "/topics");
    });

    it(`renders a given icon for that is hidden from assistive technology`, () => {
      const icon = screen.getByTestId("ds-icon");
      expect(icon).toBeVisible();
    });
  });

  describe("marks an icon as active dependent on property", () => {
    afterEach(cleanup);

    it(`doesn't mark the link as the current one for assistive technology when it is not active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          href={"/topics"}
          linkText={"Topics"}
        />
      );

      const navLink = screen.getByRole("link", { name: "Topics" });

      expect(navLink).not.toHaveAttribute("aria-current", "page");
    });

    it(`doesn't mark the link as the current one visually when it is not active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          href={"/topics"}
          linkText={"Topics"}
        />
      );

      const navLink = screen.getByRole("link", { name: "Topics" });

      expect(navLink.parentNode).not.toHaveClass("mainNavigationLinkActive");
    });

    it(`marks the link as the current one for assistive technology when is not active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          href={"/topics"}
          linkText={"Topics"}
          active={true}
        />
      );

      const navLink = screen.getByRole("link", { name: "Topics" });

      expect(navLink).toHaveAttribute("aria-current", "page");
    });

    it(`marks the link as the current one visually when it is active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          href={"/topics"}
          linkText={"Topics"}
          active={true}
        />
      );

      const navLink = screen.getByRole("link", { name: "Topics" });

      expect(navLink.parentNode).toHaveClass("mainNavigationLinkActive");
    });
  });
});
