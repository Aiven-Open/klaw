import data from "@aivenio/aquarium/dist/src/icons/console";
import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const linkText = "Go to your profile page";

describe("HeaderMenuLink.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "" as unknown as typeof data;

  describe("renders by default link with required props", () => {
    beforeEach(() => {
      customRender(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
        />,
        { memoryRouter: true }
      );
    });

    afterEach(cleanup);

    it(`renders a link with text dependent on a given property`, () => {
      const navLink = screen.getByRole("link", {
        name: linkText,
      });

      expect(navLink).toBeVisible();
    });

    it(`renders a href for that link dependent on a given property`, () => {
      const navLink = screen.getByRole("link", {
        name: linkText,
      });
      expect(navLink).toHaveAttribute("href", "/myProfile");
    });

    it(`renders an Icon`, async () => {
      const icon = screen.getByTestId("ds-icon");

      expect(icon).toBeVisible();
    });

    it(`triggers the rendering of a Tooltip on mouse hover`, async () => {
      const navLink = screen.getByRole("link", {
        name: linkText,
      });
      const tooltipBeforeHover = screen.queryByRole("tooltip");

      expect(tooltipBeforeHover).toBeNull();

      await userEvent.hover(navLink);

      const tooltipAfterHover = screen.getByRole("tooltip");

      expect(tooltipAfterHover).toBeVisible();
      expect(tooltipAfterHover).toHaveTextContent(linkText);

      // Assert the tooltip disappears after hover event stops
      await userEvent.unhover(navLink);
      expect(tooltipBeforeHover).toBeNull();
    });

    it(`triggers the rendering of a Tooltip on tab navigation`, async () => {
      const navLink = screen.getByRole("link", {
        name: linkText,
      });
      const tooltipBeforeFocus = screen.queryByRole("tooltip");

      expect(tooltipBeforeFocus).toBeNull();

      await tabNavigateTo({ targetElement: navLink });

      const tooltipAfterFocus = screen.getByRole("tooltip");

      expect(tooltipAfterFocus).toBeVisible();
      expect(tooltipAfterFocus).toHaveTextContent(linkText);

      // Assert the tooltip disappears after losing focus
      await userEvent.tab();
      expect(tooltipBeforeFocus).toBeNull();
    });

    it(`renders a hidden child with the tooltip text for assistive technology`, async () => {
      const hiddenText = screen.getByText(linkText);

      expect(hiddenText).toHaveClass("visually-hidden");
    });
  });

  describe("renders an link with additional attributes dependent on props", () => {
    afterEach(cleanup);

    it(`does not add a rel attribute by default`, () => {
      customRender(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
        />,
        { memoryRouter: true }
      );

      const navLink = screen.getByRole("link", {
        name: linkText,
      });

      expect(navLink).not.toHaveAttribute("rel");
    });

    it(`adds a rel attribute with a given value`, () => {
      customRender(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
          rel={"noreferrer"}
        />,
        { memoryRouter: true }
      );

      const navLink = screen.getByRole("link", {
        name: linkText,
      });

      expect(navLink).toHaveAttribute("rel", "noreferrer");
    });
  });
});
