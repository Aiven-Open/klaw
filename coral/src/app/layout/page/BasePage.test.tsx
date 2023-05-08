import { cleanup, render, screen, within } from "@testing-library/react";
import { BasePage } from "src/app/layout/page/BasePage";

describe("BasePage.tsx", () => {
  const contentElement = <div data-testid={"content-element"} />;
  const headerContentElement = <div data-testid={"header-content-element"} />;
  const sidebarElement = <div data-testid={"sidebar-element"} />;

  describe("renders a basic page layout with dedicated, required slots", () => {
    beforeAll(() => {
      render(<BasePage content={contentElement} />);
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
  });

  describe("renders additional elements as slots based on props", () => {
    beforeAll(() => {
      render(
        <BasePage
          headerContent={headerContentElement}
          sidebar={sidebarElement}
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

    it("renders a given component as sidebar content", () => {
      const sidebar = screen.getByTestId("sidebar-element");

      expect(sidebar).toBeVisible();
    });
  });
});
