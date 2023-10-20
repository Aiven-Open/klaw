import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchFilter.tsx", () => {
  const label = "Name of field";
  const placeholder = "Example of something to search";
  const description = "This is a description displayed under the field";
  const ariaDescription = "This is a description for screen readers";

  describe("renders default view when no query is set", () => {
    beforeAll(async () => {
      const WrappedSearchFilter = withFiltersContext({
        element: (
          <SearchFilter
            label={label}
            placeholder={placeholder}
            description={description}
            ariaDescription={ariaDescription}
          />
        ),
      });

      customRender(<WrappedSearchFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("renders a search input", () => {
      const searchInput = screen.getByRole("search", { name: label });

      expect(searchInput).toBeEnabled();
    });

    it("shows accessible label", () => {
      const searchInput = screen.getByRole("search");

      expect(searchInput).toHaveAccessibleName(label);
    });

    it("shows a placeholder dependent on prop", () => {
      const searchInput = screen.getByRole("search", { name: label });

      expect(searchInput).toHaveAttribute("placeholder", placeholder);
    });

    it("shows a description dependent on description and ariaDescriptionprops", () => {
      const searchInput = screen.getByRole("search", { name: label });

      expect(searchInput).toHaveAccessibleDescription(
        `${description} ${ariaDescription}`
      );
    });
  });

  describe("sets the field value on a query param", () => {
    const topicTest = "topic-test";

    beforeEach(async () => {
      const routePath = `/topics?search=${topicTest}`;
      const WrappedSearchFilter = withFiltersContext({
        element: (
          <SearchFilter
            label={label}
            placeholder={placeholder}
            description={description}
            ariaDescription={ariaDescription}
          />
        ),
      });

      customRender(<WrappedSearchFilter />, {
        memoryRouter: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("renders `topic-test` as field value", async () => {
      const searchInput = await screen.findByRole("search");
      expect(searchInput).toHaveValue(topicTest);
    });
  });

  describe("handles user typing a search", () => {
    beforeEach(async () => {
      const WrappedSearchFilter = withFiltersContext({
        element: (
          <SearchFilter
            label={label}
            placeholder={placeholder}
            description={description}
            ariaDescription={ariaDescription}
          />
        ),
      });

      customRender(<WrappedSearchFilter />, {
        memoryRouter: true,
      });
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("sets the topic the user choose as active option", async () => {
      const searchInput = screen.getByRole("search", { name: label });

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");
    });
  });

  describe("updates the search param to preserve topic in url", () => {
    beforeEach(async () => {
      const WrappedSearchFilter = withFiltersContext({
        element: (
          <SearchFilter
            label={label}
            placeholder={placeholder}
            description={description}
            ariaDescription={ariaDescription}
          />
        ),
      });

      customRender(<WrappedSearchFilter />, {
        browserRouter: true,
      });
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      jest.resetAllMocks();
      cleanup();
    });

    it("shows no search param by default", async () => {
      expect(window.location.search).toEqual("");
    });

    it("sets `testing` and `page=1` as search param when user types in search input", async () => {
      const searchInput = screen.getByRole("search", { name: label });

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");

      await waitFor(() => {
        expect(window.location.search).toEqual(`?search=testing&page=1`);
      });
    });
  });
});
