import { LoadingTable } from "src/app/features/components/layouts/LoadingTable";
import { render, cleanup, screen } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const testColumns = [
  { headerName: "First header" },
  { headerName: "Second header", width: 30 },
  { headerName: "Third header", headerInvisible: true },
  { headerName: "Fourth header", headerInvisible: true, width: 50 },
];

const testRowLength = 5;
describe("LoadingTable", () => {
  mockIntersectionObserver();

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(<LoadingTable rowLength={testRowLength} columns={testColumns} />);
    });

    afterAll(cleanup);

    it("renders a table with accessible information about loading state", () => {
      const table = screen.getByRole("table", { name: "Loading" });

      expect(table).toBeVisible();
    });

    testColumns.forEach((column) => {
      const headerName = column.headerName;
      const width = column?.width;

      it(`renders ${headerName} as column header`, () => {
        const colHead = screen.getByRole("columnheader", { name: headerName });

        expect(colHead).toBeVisible();
      });

      it(`sets a column to a certain width if given`, () => {
        const colHead = screen.getByRole("columnheader", {
          name: headerName,
        });

        if (width) {
          expect(colHead).toHaveStyle(`width: ${width}px`);
          expect(colHead).toHaveAttribute("style");
        } else {
          expect(colHead).not.toHaveAttribute("style");
        }
      });

      it("renders the given amount of rows with loading animation", () => {
        const rows = screen.getAllByRole("row");

        // the row with table headers
        expect(rows).toHaveLength(testRowLength + 1);
      });
    });
  });
});
