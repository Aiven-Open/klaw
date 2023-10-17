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
      name: "Search Topic",
    });

    expect(searchInput).toBeEnabled();
  });

  it("shows a placeholder with an example search value", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Topic",
    });

    expect(searchInput.placeholder).toEqual("my-topic-billings");
  });

  it("shows a description", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Topic",
    });

    expect(searchInput).toHaveAccessibleDescription(
      `Partial match for topic name.`
    );
  });

  it("shows a description for assistive technology", () => {
    const searchInput = screen.getByRole<HTMLInputElement>("search", {
      name: "Search Topic",
    });

    expect(searchInput).toHaveAttribute(`aria-description`);
  });
});
