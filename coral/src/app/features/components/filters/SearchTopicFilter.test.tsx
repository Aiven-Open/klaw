import { cleanup, screen } from "@testing-library/react";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchTopicFilter.tsx", () => {
  beforeAll(async () => {
    customRender(<SearchTopicFilter />, {
      memoryRouter: true,
    });
  });

  afterAll(cleanup);

  it("renders a search input", () => {
    const searchInput = screen.getByRole("search", {
      name: "Search Topic name",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder informing user about scope of search", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Topic name",
    });

    expect(searchInput.placeholder).toEqual("Search Topic name");
  });

  it("shows a description for assistive technology", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Topic name",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `Search for a partial match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`
    );
  });
});
