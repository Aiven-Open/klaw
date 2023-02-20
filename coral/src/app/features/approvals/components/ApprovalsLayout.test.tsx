import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { cleanup, render, screen } from "@testing-library/react";

const mockFilter = [
  <div data-testid={"filter-element"} key={"1"}>
    Filter element 1
  </div>,
  <div data-testid={"filter-element"} key={"2"}>
    Filter element 2
  </div>,
];
const mockTable = <div data-testid={"table-element"}>Table content</div>;

const mockPagination = <div data-testid={"pagination"} />;

describe("ApprovalsLayout", () => {
  describe("renders all necessary elements with required props", () => {
    beforeAll(() => {
      render(<ApprovalsLayout filters={mockFilter} table={mockTable} />);
    });
    afterAll(cleanup);

    it("renders all elements passed as filterElements", () => {
      const filterElements = screen.getAllByTestId("filter-element");

      expect(filterElements).toHaveLength(mockFilter.length);
    });

    mockFilter.forEach((element, index) => {
      const testId = element.props["data-testid"];
      it(`shows element with test id ${testId}`, () => {
        const filterElement = screen.getAllByTestId("filter-element")[index];

        expect(filterElement).toBeVisible();
        expect(filterElement).toHaveTextContent(element.props.children);
      });
    });

    it("renders element passed as tableElement", () => {
      const tableElement = screen.getByTestId("table-element");

      expect(tableElement).toBeVisible();
      expect(tableElement).toHaveTextContent("Table content");
    });
  });

  describe("renders an element given as pagination dependent on prop", () => {
    afterEach(cleanup);

    it("does not render a pagination element by default", () => {
      render(<ApprovalsLayout filters={mockFilter} table={mockTable} />);
      const pagination = screen.queryByTestId("pagination");

      expect(pagination).not.toBeInTheDocument();
    });

    it("renders pagination if prop is set", () => {
      render(
        <ApprovalsLayout
          filters={mockFilter}
          table={mockTable}
          pagination={mockPagination}
        />
      );
      const pagination = screen.getByTestId("pagination");

      expect(pagination).toBeVisible();
    });
  });

  describe("renders states dependent on props", () => {
    afterEach(cleanup);

    it("shows a loading state instead of the table", () => {
      render(
        <ApprovalsLayout
          filters={mockFilter}
          table={mockTable}
          isLoading={true}
        />
      );

      const table = screen.queryByRole("table");
      const skeleton = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(skeleton).toBeVisible();
    });

    it("shows an error message instead of the table", () => {
      render(
        <ApprovalsLayout
          filters={mockFilter}
          table={mockTable}
          isErrorLoading={true}
          errorMessage={{ data: { message: "Oh no, this is an error" } }}
        />
      );

      const table = screen.queryByRole("table");
      const errorMessage = screen.getByText(
        "Oh no, this is an error. Please try again later!"
      );

      expect(table).not.toBeInTheDocument();
      expect(errorMessage).toBeVisible();
    });
  });

  describe("renders the necessary DOM and CSS to create layout", () => {
    afterEach(cleanup);

    it("renders correct DOM with required props", () => {
      render(<ApprovalsLayout filters={mockFilter} table={mockTable} />);

      expect(screen).toMatchSnapshot();
    });

    it("renders correct DOM with optional pagination", () => {
      render(
        <ApprovalsLayout
          filters={mockFilter}
          table={mockTable}
          pagination={mockPagination}
        />
      );

      expect(screen).toMatchSnapshot();
    });
  });
});
