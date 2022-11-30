import Header from "src/app/layout/Header";
import { cleanup, screen, render, within } from "@testing-library/react";

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

const quickLinksNavItems = [
  "Approval requests",
  "Go to Klaw documentation page",
  "Your profile",
];

describe("Header.tsx", () => {
  beforeAll(() => {
    render(<Header />);
  });

  afterAll(cleanup);

  it("renders a header element", () => {
    const heading = screen.getByRole("banner");

    expect(heading).toBeVisible();
  });

  it("renders a link to the Klaw hompeage", () => {
    const link = screen.getByRole("link", { name: "Klaw homepage" });

    expect(link).toBeVisible();
  });

  it("renders the Klaw logo inside link hidden from assistive technology", () => {
    const link = screen.getByRole("link", { name: "Klaw homepage" });
    const logo = link.querySelector("img");

    expect(logo).toHaveAttribute("src", "/klaw_logo.png");
    expect(logo).toHaveAttribute("aria-hidden", "true");
  });

  quickLinksNavItems.forEach((item) => {
    it(`renders a link to ${item}`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const link = within(nav).getByRole("link", { name: item });

      expect(link).toBeEnabled();
    });

    it(`renders a Tooltip with an Icon, both hidden for assistive technology`, () => {
      const nav = screen.getByRole("navigation", { name: "Quick links" });
      const link = within(nav).getByRole("link", { name: item });
      const tooltip = within(link).getByTestId("tooltip");
      const icon = within(tooltip).getByTestId("ds-icon");

      expect(tooltip.parentElement).toHaveAttribute("aria-hidden", "true");
      expect(tooltip).toBeEnabled();
      expect(icon).toBeEnabled();
    });
  });
});
