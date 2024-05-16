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
    const searchInput = screen.getByRole("searchbox", {
      name: "Search Connector",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder with an example search value", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("searchbox", {
      name: "Search Connector",
    });

    expect(searchInput.placeholder).toEqual("local-file-source");
  });

  it("shows a description", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("searchbox", {
      name: "Search Connector",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `A partial match for connector name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`
    );
  });
});
