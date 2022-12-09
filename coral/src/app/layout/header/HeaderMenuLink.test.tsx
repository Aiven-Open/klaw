import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";
import { cleanup, screen, render, within } from "@testing-library/react";
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

describe("HeaderMenuLink.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "" as unknown as typeof data;

  describe("renders a default link with required props", () => {
    beforeAll(() => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={"Go to your profile page"}
        />
      );
    });

    afterAll(cleanup);

    it(`renders a link with text dependent on a given property`, () => {
      const navLink = screen.getByRole("link", {
        name: "Go to your profile page",
      });

      expect(navLink).toBeVisible();
    });

    it(`renders a href for that link dependent on a given  property`, () => {
      const navLink = screen.getByRole("link", {
        name: "Go to your profile page",
      });
      expect(navLink).toHaveAttribute("href", "/myProfile");
    });

    it(`renders a Tooltip with an Icon, both hidden for assistive technology`, () => {
      const navLink = screen.getByRole("link", {
        name: "Go to your profile page",
      });
      const tooltip = within(navLink).getByTestId("tooltip");
      const icon = within(tooltip).getByTestId("ds-icon");

      expect(tooltip.parentElement).toHaveAttribute("aria-hidden", "true");
      expect(tooltip).toBeEnabled();
      expect(icon).toBeEnabled();
    });
  });

  describe("renders additional attributes dependent on props", () => {
    afterEach(cleanup);

    it(`does not add a rel attribute by default`, () => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={"Go to your profile page"}
        />
      );

      const navLink = screen.getByRole("link", {
        name: "Go to your profile page",
      });

      expect(navLink).not.toHaveAttribute("rel");
    });

    it(`adds a rel attribute with a given value`, () => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={"Go to your profile page"}
          rel={"noreferrer"}
        />
      );

      const navLink = screen.getByRole("link", {
        name: "Go to your profile page",
      });

      expect(navLink).toHaveAttribute("rel", "noreferrer");
    });
  });
});
