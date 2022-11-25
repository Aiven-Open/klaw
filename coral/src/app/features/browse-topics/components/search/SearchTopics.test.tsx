import { render, cleanup, screen, within } from "@testing-library/react";
import { SearchTopics } from "src/app/features/browse-topics/components/search/SearchTopics";
import userEvent from "@testing-library/user-event";

describe("SearchTopics.tsx", () => {
  describe("renders all necessary elements", () => {
    const testSearchTerm = "Test";
    const mockedOnChange = jest.fn();

    beforeAll(() => {
      render(<SearchTopics onChange={mockedOnChange} value={testSearchTerm} />);
    });
    afterAll(cleanup);

    it("shows a search from", () => {
      const form = screen.getByRole("search", { name: "Topics" });

      expect(form).toBeEnabled();
    });

    it("shows a search input", () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });

      expect(input).toBeEnabled();
    });

    it("shows a given search term as value", () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });

      expect(input).toHaveValue(testSearchTerm);
    });

    it("shows a button to submit form", () => {
      const button = screen.getByRole("button", { name: "Submit search" });

      expect(button).toBeEnabled();
    });

    it("shows an icon in button that is hidden for assistive technology", () => {
      const button = screen.getByRole("button", { name: "Submit search" });
      const icon = within(button).getByTestId("visually-hidden-search-icon");

      // aria-hidden only hides elements from
      // getByRole, so `toBeVisible` is true in this case
      // even if element is hidden in the a11y tree
      expect(icon).toBeVisible();
      expect(icon).toHaveAttribute("aria-hidden", "true");
    });
  });

  describe("handles form input", () => {
    const testSearchInput = "My awesome topic";
    const mockedSearch = jest.fn();

    beforeEach(() => {
      render(<SearchTopics onChange={mockedSearch} value={""} />);
    });
    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("updates value for user input", async () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });
      expect(input).toHaveValue("");

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);
    });

    it("submits search term when user clicks button", async () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });

      await userEvent.type(input, testSearchInput);
      await userEvent.click(submitButton);

      expect(mockedSearch).toHaveBeenCalledWith(testSearchInput);
    });

    it("submits search term when user presses enter", async () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });

      await userEvent.type(input, testSearchInput);
      await userEvent.keyboard("{Enter}");

      expect(mockedSearch).toHaveBeenCalledWith(testSearchInput);
    });

    it("does not submit form when input is empty", async () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });
      const submitButton = screen.getByRole("button", {
        name: "Submit search",
      });

      await userEvent.click(input);
      await userEvent.click(submitButton);

      expect(mockedSearch).not.toHaveBeenCalled();
    });

    it("does not submit form when input is not at least one character", async () => {
      const input = screen.getByRole("searchbox", { name: "Search topics" });

      await userEvent.type(input, " ");
      await userEvent.keyboard("{Enter}");

      expect(mockedSearch).not.toHaveBeenCalled();
    });
  });
});
