import { cleanup, render, screen, within } from "@testing-library/react";
import { BasePageWithoutNav } from "src/app/layout/page/BasePageWithoutNav";

describe("BasePageWithoutNav.tsx", () => {
  const contentElement = <div data-testid={"content-element"} />;
  const headerContentElement = <div data-testid={"header-content-element"} />;

  describe("renders a basic page layout with dedicated, required slots", () => {
    beforeAll(() => {
      render(<BasePageWithoutNav content={contentElement} />);
    });

    afterAll(cleanup);

    it("renders a header element", () => {
      const heading = screen.getByRole("banner");

      expect(heading).toBeVisible();
    });

    it("renders a link to the Klaw homepage", () => {
      const link = screen.getByRole("link", { name: "Klaw homepage" });

      expect(link).toBeVisible();
    });

    it("renders the Klaw logo inside link hidden from assistive technology", () => {
      const link = screen.getByRole("link", { name: "Klaw homepage" });
      const logo = link.querySelector("img");

      expect(logo).toHaveAttribute("src");
      expect(logo).toHaveAttribute("aria-hidden", "true");
    });

    it("does not render sidebar content", () => {
      const sidebar = screen.queryByTestId("sidebar-element");

      expect(sidebar).not.toBeInTheDocument();
    });
  });

  describe("renders additional elements as slots based on props", () => {
    beforeAll(() => {
      render(
        <BasePageWithoutNav
          headerContent={headerContentElement}
          content={contentElement}
        />
      );
    });

    afterAll(cleanup);

    it("renders a given component as header content inside the header", () => {
      const header = screen.getByRole("banner");
      const headerContent = within(header).getByTestId(
        "header-content-element"
      );

      expect(headerContent).toBeVisible();
    });
  });
});
