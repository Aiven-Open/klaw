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
    const searchInput = screen.getByRole("searchbox", {
      name: "Search Topic",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder with an example search value", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("searchbox", {
      name: "Search Topic",
    });

    expect(searchInput.placeholder).toEqual("topic-billings");
  });

  it("shows a description", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("searchbox", {
      name: "Search Topic",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `Partial match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`
    );
  });
});
