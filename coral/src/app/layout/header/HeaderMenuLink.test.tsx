import data from "@aivenio/aquarium/dist/src/icons/console";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

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

const linkText = "Go to your profile page";

describe("HeaderMenuLink.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "" as unknown as typeof data;

  describe("renders a default link with required props", () => {
    beforeEach(() => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
        />
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
      const queryForToolTip = () => screen.queryByRole("tooltip");

      expect(queryForToolTip()).toBeNull();

      await userEvent.hover(navLink);

      expect(queryForToolTip()).toBeVisible();
      expect(queryForToolTip()).toHaveTextContent(linkText);

      // Assert the tooltip disappears after hover event stops
      cleanup();
      expect(queryForToolTip()).toBeNull();
    });

    it(`triggers the rendering of a Tooltip on tab navigation`, async () => {
      const navLink = screen.getByRole("link", {
        name: linkText,
      });
      const queryForToolTip = () => screen.queryByRole("tooltip");

      expect(queryForToolTip()).toBeNull();

      await tabNavigateTo({ targetElement: navLink });

      expect(queryForToolTip()).toBeVisible();
      expect(queryForToolTip()).toHaveTextContent(linkText);

      // Assert the tooltip disappears after losing focus
      cleanup();
      expect(queryForToolTip()).toBeNull();
    });

    it(`renders a hidden child with the tooltip text for assistive technology`, async () => {
      const hiddenText = screen.getByText(linkText);

      expect(hiddenText).toHaveClass("visually-hidden");
    });
  });

  describe("renders additional attributes dependent on props", () => {
    afterEach(cleanup);

    it(`does not add a rel attribute by default`, () => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
        />
      );

      const navLink = screen.getByRole("link", {
        name: linkText,
      });

      expect(navLink).not.toHaveAttribute("rel");
    });

    it(`adds a rel attribute with a given value`, () => {
      render(
        <HeaderMenuLink
          icon={mockIcon}
          href={"/myProfile"}
          linkText={linkText}
          rel={"noreferrer"}
        />
      );

      const navLink = screen.getByRole("link", {
        name: linkText,
      });

      expect(navLink).toHaveAttribute("rel", "noreferrer");
    });
  });
});
