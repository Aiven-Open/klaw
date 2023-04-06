import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SearchFilter from "src/app/features/components/filters/SearchFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("SearchFilter.tsx", () => {
  describe("renders default view when no query is set", () => {
    beforeAll(async () => {
      customRender(<SearchFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("renders a search input", () => {
      const searchInput = screen.getByRole("search");

      expect(searchInput).toBeEnabled();
    });
  });

  describe("it describes the target entity of the search", () => {
    afterEach(cleanup);
    it("provides description", () => {
      customRender(<SearchFilter />, {
        memoryRouter: true,
      });
      screen.getByPlaceholderText("Search by name");
    });
  });

  describe("sets the field value on a query param", () => {
    const topicTest = "topic-test";

    beforeEach(async () => {
      const routePath = `/topics?search=${topicTest}`;

      customRender(<SearchFilter />, {
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
      customRender(<SearchFilter />, {
        memoryRouter: true,
      });
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("sets the topic the user choose as active option", async () => {
      const searchInput = screen.getByRole("search");

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");
    });
  });

  describe("updates the search param to preserve topic in url", () => {
    beforeEach(async () => {
      customRender(<SearchFilter />, {
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
      const searchInput = screen.getByRole("search");

      await userEvent.type(searchInput, "testing");

      expect(searchInput).toHaveValue("testing");

      await waitFor(() => {
        expect(window.location.search).toEqual(`?search=testing&page=1`);
      });
    });
  });
});
