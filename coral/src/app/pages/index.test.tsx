import HomePage from "src/app/pages";
import { screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";

describe("HomePage", () => {
  beforeEach(() => {
    renderWithQueryClient(<HomePage />);
  });

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });
});
