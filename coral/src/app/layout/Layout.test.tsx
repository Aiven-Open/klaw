import Layout from "src/app/layout/Layout";
import { cleanup, screen, render, within } from "@testing-library/react";

// mock out svgs to avoid clutter
jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),
    Icon: () => null,
  };
});

describe("Layout.tsx", () => {
  const testChildren = <div data-testid={"test-children"}></div>;
  beforeAll(() => {
    render(<Layout>{testChildren}</Layout>);
  });

  afterAll(cleanup);

  it("renders the header", () => {
    const header = screen.getByRole("banner");
    expect(header).toBeVisible();
  });

  it("renders the primary navigation", () => {
    const nav = screen.getByRole("navigation", { name: "Primary navigation" });
    expect(nav).toBeVisible();
  });

  it("renders its children in the main section", () => {
    const main = screen.getByRole("main");
    const child = within(main).getByTestId("test-children");

    expect(main).toBeVisible();
    expect(child).toBeVisible();
  });
});
