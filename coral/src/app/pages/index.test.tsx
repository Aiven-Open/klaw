import { cleanup, screen } from "@testing-library/react";
import HomePage from "src/app/pages";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("HomePage", () => {
  beforeAll(() => {
    customRender(<HomePage />, { memoryRouter: true });
  });

  afterAll(cleanup);

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });
});
