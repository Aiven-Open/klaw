import SidebarNavigation from "src/app/layout/SidebarNavigation";
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
const navLinks = [
  {
    name: "Overview",
    linkTo: "http://localhost/index",
  },
  { name: "Topics", linkTo: "/" },
  { name: "Kafka Connector", linkTo: "http://localhost/kafkaConnectors" },
  { name: "Schemas", linkTo: "/" },
  { name: "User and teams", linkTo: "/" },
  { name: "Audit log", linkTo: "/" },
  { name: "Settings", linkTo: "/" },
];

describe("SidebarNavigation.tsx", () => {
  beforeAll(() => {
    render(<SidebarNavigation />);
  });

  afterAll(cleanup);

  it("renders the primary navigation", () => {
    const nav = screen.getByRole("navigation", { name: "Primary navigation" });
    expect(nav).toBeVisible();
  });

  navLinks.forEach((link) => {
    it(`renders a link for ${link.name}`, () => {
      const nav = screen.getByRole("navigation", {
        name: "Primary navigation",
      });

      const navLink = within(nav).getByRole("link", { name: link.name });
      expect(navLink).toBeVisible();
      expect(navLink).toHaveAttribute("href", link.linkTo);
    });
  });

  it(`renders icons for all nav links that are hidden from assistive technology`, () => {
    const nav = screen.getByRole("navigation", {
      name: "Primary navigation",
    });

    const icons = within(nav).getAllByTestId("ds-icon");
    expect(icons).toHaveLength(navLinks.length);
  });
});
