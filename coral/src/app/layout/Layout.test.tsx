import Layout from "src/app/layout/Layout";
import { cleanup, screen, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("Layout.tsx", () => {
  const testChildren = <div data-testid={"test-children"}></div>;
  beforeAll(() => {
    customRender(<Layout>{testChildren}</Layout>, { memoryRouter: true });
  });

  afterAll(cleanup);

  it("renders the header", () => {
    const header = screen.getByRole("banner");
    expect(header).toBeVisible();
  });

  it("renders the main navigation", () => {
    const nav = screen.getByRole("navigation", { name: "Main navigation" });
    expect(nav).toBeVisible();
  });

  it("renders its children in the main section", () => {
    const main = screen.getByRole("main");
    const child = within(main).getByTestId("test-children");

    expect(main).toBeVisible();
    expect(child).toBeVisible();
  });
});
