import { cleanup, screen } from "@testing-library/react";
import { SearchConnectorFilter } from "src/app/features/components/filters/SearchConnectorFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchConnectorFilter.tsx", () => {
  beforeAll(async () => {
    customRender(<SearchConnectorFilter />, {
      memoryRouter: true,
    });
  });

  afterAll(cleanup);

  it("renders a search input", () => {
    const searchInput = screen.getByRole("search", {
      name: "Search Connector name",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder informing user about scope of search", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Connector name",
    });

    expect(searchInput.placeholder).toEqual("Search Connector name");
  });

  it("shows a description for assistive technology", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Connector name",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `Search for a partial match for connector name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`
    );
  });
});
