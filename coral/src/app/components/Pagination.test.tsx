// @TODO remove import from `pure` when custom cleanup is merged
import { cleanup, render, within } from "@testing-library/react/pure";
import { Pagination } from "src/app/components/Pagination";
import { screen } from "@testing-library/react";

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
  describe("renders the pagination by default on page 1", () => {
    const totalPages = 5;
    beforeAll(() => {
      render(<Pagination activePage={1} totalPages={totalPages} />);
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

    it("shows no link to get the first page because that is the active one", () => {
      const link = within(getNavigationElement()).queryByRole("link", {
        name: "Go to first page",
      });

      expect(link).not.toBeInTheDocument();
    });

    it("shows no link to get the previous page because that is the active one", () => {
      const link = within(getNavigationElement()).queryByRole("link", {
        name: /Go to previos page/,
      });

      expect(link).not.toBeInTheDocument();
    });

    it("shows a link to get to the next page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: "Go to next page, page 2",
      });

      expect(link).toBeEnabled();
    });

    it("shows a link to get to the last page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: `Go to last page, page ${totalPages}`,
      });

      expect(link).toBeEnabled();
    });
  });

  describe("renders a pagination on page 3 with 6 total elements", () => {
    const activePage = 3;
    const totalPages = 6;
    beforeAll(() => {
      render(<Pagination activePage={activePage} totalPages={totalPages} />);
    });

    afterAll(cleanup);

    it("shows a link to get the first page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: "Go to first page",
      });

      expect(link).toBeEnabled();
    });

    it("shows a link to get the previous page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: `Go to previous page, page ${activePage - 1}`,
      });

      expect(link).toBeEnabled();
    });

    it("shows a link to get to the next page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: `Go to next page, page ${activePage + 1}`,
      });

      expect(link).toBeVisible();
    });

    it("shows a link to get to the last page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: `Go to last page, page ${totalPages}`,
      });

      expect(link).toBeVisible();
    });
  });

  describe("renders a pagination on the last page with 4 total elements", () => {
    const activePage = 4;
    const totalPages = 4;
    beforeAll(() => {
      render(<Pagination activePage={activePage} totalPages={totalPages} />);
    });

    afterAll(cleanup);

    it("shows a link to get the first page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: "Go to first page",
      });

      expect(link).toBeEnabled();
    });

    it("shows a link to get the previous page", () => {
      const link = within(getNavigationElement()).getByRole("link", {
        name: `Go to previous page, page ${activePage - 1}`,
      });

      expect(link).toBeEnabled();
    });

    it("shows no link to get to the next page because that is the active one", () => {
      const link = within(getNavigationElement()).queryByRole("link", {
        name: /Go to next page, page/,
      });

      expect(link).not.toBeInTheDocument();
    });

    it("shows no link to get to the last page because that is the active one", () => {
      const link = within(getNavigationElement()).queryByRole("link", {
        name: /Go to last page, page/,
      });

      expect(link).not.toBeInTheDocument();
    });
  });
});
