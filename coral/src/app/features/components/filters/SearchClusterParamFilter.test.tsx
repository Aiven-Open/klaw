import { cleanup, screen } from "@testing-library/react";
import { SearchClusterParamFilter } from "src/app/features/components/filters/SearchClusterParamFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchClusterParamFilter.tsx", () => {
  beforeAll(async () => {
    customRender(<SearchClusterParamFilter />, {
      memoryRouter: true,
    });
  });

  afterAll(cleanup);

  it("renders a search input", () => {
    const searchInput = screen.getByRole("search", {
      name: "Search Cluster parameters",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder with an example search value", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Cluster parameters",
    });

    expect(searchInput.placeholder).toEqual("kafkaconnect");
  });

  it("shows a description", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Cluster parameters",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `Partial match for: Cluster name, bootstrap server and protocol. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`
    );
  });
});
