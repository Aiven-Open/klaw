import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchFilter.tsx", () => {
  const placeholder = "Search something";
  const description = "This is a description for screenreaders";

  describe("renders default view when no query is set", () => {
    beforeEach(async () => {
      customRender(
        <SearchFilter placeholder={placeholder} description={description} />,
        {
          memoryRouter: true,
        }
      );
    });

    afterEach(cleanup);

    it("renders a search input", () => {
      const searchInput = screen.getByRole("search", { name: placeholder });

      expect(searchInput).toBeEnabled();
    });

    it("shows placeholder as accessible label", () => {
      const searchInput = screen.getByRole("search");

      expect(searchInput).toHaveAccessibleName(placeholder);
    });

    it("shows a placeholder dependent on prop", () => {
      const searchInput = screen.getByRole("search", { name: placeholder });

      expect(searchInput).toHaveAttribute("placeholder", placeholder);
    });

    it("shows a description for AT dependent on prop", () => {
      const searchInput = screen.getByRole("search", { name: placeholder });

      expect(searchInput).toHaveAccessibleDescription(description);
    });
  });

  describe("sets the field value on a query param", () => {
    const topicTest = "topic-test";

    beforeEach(async () => {
      const routePath = `/topics?search=${topicTest}`;

      customRender(
        <SearchFilter placeholder={placeholder} description={description} />,
        {
          memoryRouter: true,
          customRoutePath: routePath,
        }
      );
    });

    afterEach(() => {
      vi.resetAllMocks();
      cleanup();
    });

    it("renders `topic-test` as field value", async () => {
      const searchInput = await screen.findByRole("search");
      expect(searchInput).toHaveValue(topicTest);
    });
  });

  describe("handles user typing a search", () => {
    beforeEach(async () => {
      customRender(
        <SearchFilter placeholder={placeholder} description={description} />,
        {
          memoryRouter: true,
        }
      );
    });

    afterEach(() => {
      vi.resetAllMocks();
      cleanup();
    });

    it("sets the topic the user choose as active option", async () => {
      const searchInput = screen.getByRole("search", { name: placeholder });

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");
    });
  });

  describe("updates the search param to preserve topic in url", () => {
    beforeEach(async () => {
      customRender(
        <SearchFilter placeholder={placeholder} description={description} />,
        {
          browserRouter: true,
        }
      );
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      vi.resetAllMocks();
      cleanup();
    });

    it("shows no search param by default", async () => {
      expect(window.location.search).toEqual("");
    });

    it("sets `testing` and `page=1` as search param when user types in search input", async () => {
      const searchInput = screen.getByRole("search", { name: placeholder });

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");

      await waitFor(() => {
        expect(window.location.search).toEqual(`?search=testing&page=1`);
      });
    });
  });
});
