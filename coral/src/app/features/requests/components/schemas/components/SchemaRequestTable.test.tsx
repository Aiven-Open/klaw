import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { cleanup, render, screen, within } from "@testing-library/react";
import { SchemaRequestTable } from "src/app/features/requests/components/schemas/components/SchemaRequestTable";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";
import { mockedSchemaRequests } from "src/app/features/requests/components/schemas/utils/mocked-schema-requests";

const schemaRequests = [...mockedSchemaRequests];
const deletableRequests = schemaRequests.filter((entry) => entry.deletable);

describe("SchemaRequestTable", () => {
  beforeAll(mockIntersectionObserver);

  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Type", relatedField: "requestOperationType" },
    { columnHeader: "Requested by", relatedField: "username" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Delete", relatedField: null },
  ];

  describe("shows information that table is empty when requests are empty", () => {
    beforeAll(() => {
      render(<SchemaRequestTable requests={[]} />);
    });

    afterAll(cleanup);

    it("informs user there are no requests matching their criteria", () => {
      const text = screen.getByText("No Schema request matched your criteria.");

      expect(text).toBeVisible();
    });
  });

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(<SchemaRequestTable requests={schemaRequests} />);
    });

    afterAll(cleanup);

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(schemaRequests.length + 1);
    });

    it("shows a button to view details for every row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: "View",
      });

      expect(buttons).toHaveLength(schemaRequests.length);
    });

    it("shows an enabled button for every row where request is 'deletable'", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: "Delete",
      });

      const disabledButtons = buttons.filter((button) => {
        return (button as HTMLButtonElement).disabled;
      });

      expect(disabledButtons).toHaveLength(
        schemaRequests.length - deletableRequests.length
      );
      expect(buttons.length - disabledButtons.length).toEqual(
        deletableRequests.length
      );
    });

    schemaRequests.forEach((request) => {
      //@TODO buttons don't have discernible names right now
      // should be fixed for accessibility
      xit(`shows a button to show the detailed schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });

    deletableRequests.forEach((request) => {
      //@TODO buttons don't have discernible names right now
      // should be fixed for accessibility
      xit(`shows a button to delete schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `Delete schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      render(<SchemaRequestTable requests={schemaRequests} />);
    });

    afterAll(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Schema requests",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * schemaRequests.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      if (column.relatedField) {
        schemaRequests.forEach((request) => {
          it(`shows field ${column.relatedField} for request number ${request.req_no}`, () => {
            const table = screen.getByRole("table", {
              name: "Schema requests",
            });

            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            //@ts-ignore
            const field = request[column.relatedField];

            let text = field;
            if (column.columnHeader === "Requested on") {
              text = `${field}${"\u00A0"}UTC`;
            }

            if (column.columnHeader === "Status") {
              text = requestStatusNameMap[field as RequestStatus];
            }

            if (column.columnHeader === "Type") {
              text = requestOperationTypeNameMap[field as RequestOperationType];
            }
            const cell = within(table).getByRole("cell", { name: text });

            expect(cell).toBeVisible();
          });
        });
      }
    });
  });
});
