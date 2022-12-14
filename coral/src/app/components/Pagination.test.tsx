import { cleanup, render, within, screen } from "@testing-library/react";
import { Pagination } from "src/app/components/Pagination";
import userEvent from "@testing-library/user-event";

const mockIconRender = jest.fn();
// mocks out Icon component to avoid clutter
// Icon is used purely decoratively
jest.mock("@aivenio/aquarium", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/aquarium"),
    Icon: () => mockIconRender(),
  };
});

function getNavigationElement() {
  return screen.getByRole("navigation", { name: /Pagination/ });
}

describe("Pagination.tsx", () => {
  describe("renders the pagination by default with active page `1`", () => {
    const activePage = 1;
    const totalPages = 5;

    describe("shows all necessary elements", () => {
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

      it("shows a navigation element", () => {
        const navigation = getNavigationElement();

        expect(navigation).toBeVisible();
      });

      it("gives information about current and total pages for assistive technology", () => {
        const navigation = getNavigationElement();

        expect(navigation).toHaveAccessibleName(
          "Pagination navigation, you're on page 1 of 5"
        );
      });

      it("shows list for navigation items", () => {
        const list = within(getNavigationElement()).getByRole("list");

        expect(list).toBeVisible();
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

      it("shows visual only information about current page and total pages", () => {
        const text = within(getNavigationElement()).getByText("Page 1 of 5");

        expect(text).toBeVisible();
        expect(text).toHaveAttribute("aria-hidden", "true");
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

    describe("handles updating the pages`", () => {
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

      it('does not update page when user clicks "Go to first page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: "Go to first page",
          hidden: true,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).not.toHaveBeenCalled();
      });

      it('does not update page when user clicks "Go to previous page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to previous page/,
          hidden: true,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).not.toHaveBeenCalled();
      });

      it('calls "setActivePage" with "3" if user clicks "Go to next page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to next page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(2);
      });

      it('calls "setActivePage" with "4" if user clicks "Go to last page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to last page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(5);
      });
    });

    describe("enables keyboard only navigation", () => {
      const mockSetActivePage = jest.fn();
      beforeEach(() => {
        render(
          <Pagination
            activePage={activePage}
            totalPages={totalPages}
            setActivePage={mockSetActivePage}
          />
        );
        const navigation = getNavigationElement();
        navigation.focus();
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it('focuses "Go to next page" button first', async () => {
        const button = screen.getByRole("button", { name: /Go to next page/ });

        await userEvent.tab();
        expect(button).toHaveFocus();
      });

      it('updates to page 2 when user presses enter on "Go to next page" button', async () => {
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(2);
      });

      it('focuses "Go to last page" button next', async () => {
        const button = screen.getByRole("button", { name: /Go to last page/ });
        await userEvent.tab();
        await userEvent.tab();

        expect(button).toHaveFocus();
      });

      it('updates to page 5 when user presses enter on "Go to last page" button', async () => {
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(5);
      });
    });
  });

  describe("renders a pagination on page 3 with 6 total elements", () => {
    const activePage = 3;
    const totalPages = 6;

    describe("shows all necessary elements", () => {
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

      it("gives information about current and total pages for assistive technology", () => {
        const navigation = getNavigationElement();

        expect(navigation).toHaveAccessibleName(
          "Pagination navigation, you're on page 3 of 6"
        );
      });

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

      it("shows visual only information about current page and total pages", () => {
        const text = within(getNavigationElement()).getByText("Page 3 of 6");

        expect(text).toBeVisible();
        expect(text).toHaveAttribute("aria-hidden", "true");
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

    describe("handles updating the pages`", () => {
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

      it('calls "setActivePage" with "1" if user clicks "Go to first page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: "Go to first page",
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(1);
      });

      it('calls "setActivePage" with "2" if user clicks "Go to previous page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to previous page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(2);
      });

      it('calls "setActivePage" with "4" if user clicks "Go to next page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to next page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(4);
      });

      it('calls "setActivePage" with "6" if user clicks "Go to last page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to last page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(6);
      });
    });

    describe("enables keyboard only navigation", () => {
      const mockSetActivePage = jest.fn();
      beforeEach(() => {
        render(
          <Pagination
            activePage={activePage}
            totalPages={totalPages}
            setActivePage={mockSetActivePage}
          />
        );
        const navigation = getNavigationElement();
        navigation.focus();
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it('focuses "Go to first page" button first', async () => {
        const button = screen.getByRole("button", { name: /Go to first page/ });

        await userEvent.tab();
        expect(button).toHaveFocus();
      });

      it('updates to page 1 when user presses enter on "Go to first page"', async () => {
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(1);
      });

      it('focuses "Go to previous page" button next', async () => {
        const button = screen.getByRole("button", {
          name: /Go to previous page/,
        });
        await userEvent.tab();
        await userEvent.tab();

        expect(button).toHaveFocus();
      });

      it('updates to page 1 when user presses enter on "Go to previous page"', async () => {
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(2);
      });

      it('focuses "Go to next page" button next', async () => {
        const button = screen.getByRole("button", { name: /Go to next page/ });
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();

        expect(button).toHaveFocus();
      });

      it('updates to page 4 when user presses enter on "Go to next page" button', async () => {
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(4);
      });

      it('focuses "Go to last page" button last', async () => {
        const button = screen.getByRole("button", { name: /Go to last page/ });
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();

        expect(button).toHaveFocus();
      });

      it('updates to page 6 when user presses enter on "Go to last page" button', async () => {
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(6);
      });
    });
  });

  describe("renders a pagination on the last page with 4 total elements", () => {
    const activePage = 4;
    const totalPages = 4;

    describe("shows all necessary elements", () => {
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

      it("gives information about current and total pages for assistive technology", () => {
        const navigation = getNavigationElement();

        expect(navigation).toHaveAccessibleName(
          "Pagination navigation, you're on page 4 of 4"
        );
      });

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

      it("shows visual only information about current page and total pages", () => {
        const text = within(getNavigationElement()).getByText("Page 4 of 4");

        expect(text).toBeVisible();
        expect(text).toHaveAttribute("aria-hidden", "true");
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

    describe("handles updating the pages`", () => {
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

      it('calls "setActivePage" with "1" if user clicks "Go to first page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: "Go to first page",
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(1);
      });

      it('calls "setActivePage" with "3" if user clicks "Go to previous page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to previous page/,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).toHaveBeenCalledWith(3);
      });

      it('does not update page when user clicks "Go to next page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to next page/,
          hidden: true,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).not.toHaveBeenCalled();
      });

      it('does not update page when user clicks "Go to last page"', async () => {
        const button = within(getNavigationElement()).getByRole("button", {
          name: /Go to last page/,
          hidden: true,
        });
        await userEvent.click(button);

        expect(mockedSetActivePage).not.toHaveBeenCalled();
      });
    });

    describe("enables keyboard only navigation", () => {
      const mockSetActivePage = jest.fn();
      beforeEach(() => {
        render(
          <Pagination
            activePage={activePage}
            totalPages={totalPages}
            setActivePage={mockSetActivePage}
          />
        );
        const navigation = getNavigationElement();
        navigation.focus();
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it('focuses "Go to first page" button first', async () => {
        const button = screen.getByRole("button", { name: /Go to first page/ });

        await userEvent.tab();
        expect(button).toHaveFocus();
      });

      it('updates to page 1 when user presses enter on "Go to first page"', async () => {
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(1);
      });

      it('focuses "Go to previous page" button next', async () => {
        const button = screen.getByRole("button", {
          name: /Go to previous page/,
        });
        await userEvent.tab();
        await userEvent.tab();

        expect(button).toHaveFocus();
      });

      it('updates to page 3 when user presses enter on "Go to previous page"', async () => {
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.keyboard("{Enter}");

        expect(mockSetActivePage).toHaveBeenCalledWith(3);
      });

      it('does not focus on "Go to next page"', async () => {
        const button = screen.getByRole("button", {
          name: /Go to next page/,
          hidden: true,
        });
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();

        expect(button).not.toHaveFocus();
        expect(document.body).toHaveFocus();
      });

      it('does not focus on "Go to last page"', async () => {
        const button = screen.getByRole("button", {
          name: /Go to next page/,
          hidden: true,
        });
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();
        await userEvent.tab();

        expect(button).not.toHaveFocus();
        expect(
          screen.getByRole("button", { name: /Go to first page/ })
        ).toHaveFocus();
      });
    });
  });
});
