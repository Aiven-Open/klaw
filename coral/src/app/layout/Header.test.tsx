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

// "http://localhost/" comes from window.location.origin
// that represents the Angular app
const quickLinksNavItems = [
  { name: "Go to approval requests", linkTo: "http://localhost/execTopics" },
  {
    name: "Go to Klaw documentation page",
    linkTo: "https://www.klaw-project.io/docs",
  },
  { name: "Go to your profile", linkTo: "http://localhost/myProfile" },
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
      const link = within(nav).getByRole("link", { name: item.name });

      expect(link).toBeEnabled();
      expect(link).toHaveAttribute("href", item.linkTo);
    });
  });

  it("renders all links in the header menu", () => {
    const nav = screen.getByRole("navigation", { name: "Quick links" });
    const links = within(nav).getAllByRole("link");

    expect(links).toHaveLength(quickLinksNavItems.length);
  });
});
