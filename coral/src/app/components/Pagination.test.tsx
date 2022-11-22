import { cleanup, render, within, screen } from "@testing-library/react";
import { Pagination } from "src/app/components/Pagination";
import userEvent from "@testing-library/user-event";

const mockIconRender = jest.fn();
// mocks out Icon component to avoid clutter
// Icon is used purely decoratively
jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),
    Icon: () => mockIconRender(),
  };
});

function getNavigationElement() {
  return screen.getByRole("navigation", { name: "Pagination" });
}

describe("Pagination.tsx", () => {
  describe("renders the pagination by default with active page `1`", () => {
    const totalPages = 5;

    beforeAll(() => {
      render(
        <Pagination
          activePage={1}
          totalPages={totalPages}
          setActivePage={jest.fn()}
        />
      );
    });

    afterAll(cleanup);

    it("shows a navigation element", () => {
      const navigation = getNavigationElement();

      expect(navigation).toBeVisible();
    });

    it("shows list for navigation items", () => {
      const list = within(getNavigationElement()).getByRole("list");

      expect(list).toBeVisible();
    });

    it("renders visually hidden text to inform assistive tech about active and all pages", () => {
      const info = within(getNavigationElement()).getByText(
        `You are on page 1 of ${totalPages}`
      );

      expect(info).toBeVisible();
    });

    it("shows no button to get the first page for assistive technology", () => {
      const button = within(getNavigationElement()).queryByRole("button", {
        name: "Go to first page",
      });

      expect(button).not.toBeInTheDocument();
    });

    it("disables button to get the first page", () => {
      const button = within(getNavigationElement()).queryByRole("button", {
        name: "Go to first page",
        hidden: true,
      });

      expect(button).toBeDisabled();
    });

    it("shows no button to get the previous page for assistive technology", () => {
      const button = within(getNavigationElement()).queryByRole("button", {
        name: /Go to previous page/,
      });

      expect(button).not.toBeInTheDocument();
    });

    it("disables button to get the previous page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: /Go to previous page/,
        hidden: true,
      });

      expect(button).toBeDisabled();
    });

    it("shows a button to get to the next page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: "Go to next page, page 2",
      });

      expect(button).toBeEnabled();
    });

    it("shows a button to get to the last page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to last page, page ${totalPages}`,
      });

      expect(button).toBeEnabled();
    });

    it("renders all icons", () => {
      // makes sure that the right amount of icons has been used
      // will break if icons are missing / not used
      // does NOT check if the right icons are rendered
      expect(mockIconRender).toHaveBeenCalledTimes(4);
    });
  });

  describe("renders a pagination on page 3 with 6 total elements", () => {
    const activePage = 3;
    const totalPages = 6;

    beforeAll(() => {
      render(
        <Pagination
          activePage={activePage}
          totalPages={totalPages}
          setActivePage={jest.fn()}
        />
      );
    });

    afterAll(cleanup);

    it("shows a button to get the first page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: "Go to first page",
      });

      expect(button).toBeEnabled();
    });

    it("shows a button to get the previous page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to previous page, page ${activePage - 1}`,
      });

      expect(button).toBeEnabled();
    });

    it("shows a button to get to the next page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to next page, page ${activePage + 1}`,
      });

      expect(button).toBeVisible();
    });

    it("shows a button to get to the last page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to last page, page ${totalPages}`,
      });

      expect(button).toBeVisible();
    });
  });

  describe("renders a pagination on the last page with 4 total elements", () => {
    const activePage = 4;
    const totalPages = 4;

    beforeAll(() => {
      render(
        <Pagination
          activePage={activePage}
          totalPages={totalPages}
          setActivePage={jest.fn()}
        />
      );
    });

    afterAll(cleanup);

    it("shows a button to get the first page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: "Go to first page",
      });

      expect(button).toBeEnabled();
    });

    it("shows a button to get the previous page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to previous page, page ${activePage - 1}`,
      });

      expect(button).toBeEnabled();
    });

    it("shows no button to get to the next page for assistive technology", () => {
      const button = within(getNavigationElement()).queryByRole("button", {
        name: /Go to next page, page/,
      });

      expect(button).not.toBeInTheDocument();
    });

    it("disables button to get to next page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: /Go to next page, page/,
        hidden: true,
      });

      expect(button).toBeDisabled();
    });

    it("shows no button to get to the last page  for assistive technology", () => {
      const button = within(getNavigationElement()).queryByRole("button", {
        name: /Go to last page, page/,
      });

      expect(button).not.toBeInTheDocument();
    });

    it("disables button to get to last page", () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: /Go to next page, page/,
        hidden: true,
      });

      expect(button).toBeDisabled();
    });
  });

  describe("handles updating the page when active page is `2`", () => {
    const activePage = 2;
    const totalPages = 4;

    const mockedSetActivePage = jest.fn();
    beforeEach(() => {
      render(
        <Pagination
          activePage={activePage}
          totalPages={totalPages}
          setActivePage={mockedSetActivePage}
        />
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("calls `setActivePage` with `1` if user clicks `Go to page`", async () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: "Go to first page",
      });
      await userEvent.click(button);

      expect(mockedSetActivePage).toHaveBeenCalledWith(1);
    });

    it("calls `setActivePage` with `1` if user clicks `Go to previous page`", async () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: `Go to previous page, page ${activePage - 1}`,
      });
      await userEvent.click(button);

      expect(mockedSetActivePage).toHaveBeenCalledWith(1);
    });

    it("calls `setActivePage` with `3` if user clicks `Go to next page`", async () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: /Go to next page, page/,
      });
      await userEvent.click(button);

      expect(mockedSetActivePage).toHaveBeenCalledWith(3);
    });

    it("calls `setActivePage` with `4` if user clicks `Go to last page`", async () => {
      const button = within(getNavigationElement()).getByRole("button", {
        name: /Go to last page, page/,
      });
      await userEvent.click(button);

      expect(mockedSetActivePage).toHaveBeenCalledWith(4);
    });
  });
});
