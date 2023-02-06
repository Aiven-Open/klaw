import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";
import { cleanup, render, screen } from "@testing-library/react";
import data from "@aivenio/aquarium/dist/src/icons/console";
import { MemoryRouter } from "react-router-dom";
import { Routes } from "src/app/router_utils";

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

describe("MainNavigationLink.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "fake-icon" as unknown as typeof data;

  describe("renders a default link with required props with a string as `to`", () => {
    beforeAll(() => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          to={"/some-klaw-link"}
          linkText={"Link back to Klaw"}
        />
      );
    });

    afterAll(cleanup);

    it(`renders a link with text dependent on a given property`, () => {
      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });

      expect(navLink).toBeVisible();
    });

    it(`renders a href for that link dependent on a given  property`, () => {
      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });
      expect(navLink).toHaveAttribute("href", "/some-klaw-link");
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
          to={"/some-klaw-link"}
          linkText={"Link back to Klaw"}
        />
      );

      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });

      expect(navLink).not.toHaveAttribute("aria-current", "page");
    });

    it(`doesn't mark the link as the current one visually when it is not active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          to={"/some-klaw-link"}
          linkText={"Link back to Klaw"}
        />
      );

      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });

      expect(navLink.parentNode).not.toHaveClass("mainNavigationLinkActive");
    });

    it(`marks the link as the current one for assistive technology when is not active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          to={"/some-klaw-link"}
          linkText={"Link back to Klaw"}
          active={true}
        />
      );

      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });

      expect(navLink).toHaveAttribute("aria-current", "page");
    });

    it(`marks the link as the current one visually when it is active`, () => {
      render(
        <MainNavigationLink
          icon={mockIcon}
          to={"/some-klaw-link"}
          linkText={"Link back to Klaw"}
          active={true}
        />
      );

      const navLink = screen.getByRole("link", { name: "Link back to Klaw" });

      expect(navLink.parentNode).toHaveClass("mainNavigationLinkActive");
    });
  });

  describe("renders a react router <Link> when `to` belongs to `Routes`", () => {
    afterAll(cleanup);
    it("renders correct link content", () => {
      render(
        <MemoryRouter initialEntries={["/users/mjackson"]}>
          <MainNavigationLink
            icon={mockIcon}
            to={Routes.TOPICS}
            linkText={"Topics"}
            active={true}
          />
        </MemoryRouter>
      );
      const link = screen.getByRole("link", { name: "Topics" });
      expect(link).toHaveAttribute("href", "/topics");
      expect(link).toHaveAttribute("aria-current", "page");
    });
  });
});
