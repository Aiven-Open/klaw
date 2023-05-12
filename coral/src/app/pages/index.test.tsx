import { cleanup, screen } from "@testing-library/react";
import HomePage from "src/app/pages";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("HomePage", () => {
  beforeEach(() => {
    customRender(<HomePage />, { memoryRouter: true, queryClient: true });
  });

  afterEach(cleanup);

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });
});
