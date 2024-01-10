import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { cleanup, screen, within } from "@testing-library/react";
import { SchemaRequestTable } from "src/app/features/requests/schemas/components/SchemaRequestTable";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import { RequestOperationType, RequestStatus } from "src/domain/requests";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";
import { mockedApiResponses } from "src/app/features/requests/schemas/utils/mocked-api-responses";
import { userEvent } from "@testing-library/user-event";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const schemaRequests = [...mockedApiResponses];
const deletableRequests = schemaRequests.filter((entry) => entry.deletable);

const showDetailsMock = jest.fn();
const showDeleteDialogMock = jest.fn();

describe("SchemaRequestTable", () => {
  beforeAll(mockIntersectionObserver);

  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Request type", relatedField: "requestOperationType" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Delete", relatedField: null },
  ];

  describe("shows information that table is empty when requests are empty", () => {
    beforeAll(() => {
      customRender(
        <SchemaRequestTable
          requests={[]}
          showDetails={showDetailsMock}
          showDeleteDialog={showDeleteDialogMock}
          ariaLabel={"Schema requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    it("informs user there are no requests matching their criteria", () => {
      const text = screen.getByText("No Schema request matched your criteria.");

      expect(text).toBeVisible();
    });
  });

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      customRender(
        <SchemaRequestTable
          requests={schemaRequests}
          showDetails={showDetailsMock}
          showDeleteDialog={showDeleteDialogMock}
          ariaLabel={"Schema requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(schemaRequests.length + 1);
    });

    it("shows a button to view details for every row", () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /View schema request for/,
      });

      expect(buttons).toHaveLength(schemaRequests.length);
    });

    it("shows an enabled button for every row where request is 'deletable'", () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /Delete schema request for/,
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
      it(`shows a button to show the detailed schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });

    deletableRequests.forEach((request) => {
      it(`shows a button to delete schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Delete schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      customRender(
        <SchemaRequestTable
          requests={schemaRequests}
          showDetails={showDetailsMock}
          showDeleteDialog={showDeleteDialogMock}
          ariaLabel={"Schema requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Schema requests, page 1 of 10",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * schemaRequests.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests, page 1 of 10",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      if (column.relatedField) {
        schemaRequests.forEach((request) => {
          const isUserLink = column.columnHeader === "Requested by";
          const isFormattedDate = column.columnHeader === "Requested on";
          const isStatus = column.columnHeader === "Status";
          const isRequestType = column.columnHeader === "Request type";

          it(`shows field ${column.relatedField} for request number ${request.req_no}`, () => {
            const table = screen.getByRole("table", {
              name: "Schema requests, page 1 of 10",
            });

            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            //@ts-ignore
            const field = request[column.relatedField];

            if (isUserLink) {
              const cell = within(table).getByRole("cell", { name: field });
              const link = within(cell).getByRole("link", { name: field });

              expect(link).toBeVisible();
              expect(link).toHaveAttribute("href", "/configuration/users");

              // formatting for readability
            } else if (isFormattedDate) {
              const cell = within(table).getByRole("cell", {
                name: `${field}${"\u00A0"}UTC`,
              });

              expect(cell).toBeVisible();

              // formatting for readability
            } else if (isStatus) {
              const cell = within(table).getByRole("cell", {
                name: requestStatusNameMap[field as RequestStatus],
              });

              expect(cell).toBeVisible();

              // formatting for readability
            } else if (isRequestType) {
              const cell = within(table).getByRole("cell", {
                name: requestOperationTypeNameMap[
                  field as RequestOperationType
                ],
              });

              expect(cell).toBeVisible();

              // formatting for readability
            } else {
              const cell = within(table).getByRole("cell", { name: field });
              expect(cell).toBeVisible();
            }
          });
        });
      }
    });
  });

  describe("triggers opening of a modal with all details if user clicks button 'View'", () => {
    beforeEach(() => {
      customRender(
        <SchemaRequestTable
          requests={schemaRequests}
          showDetails={showDetailsMock}
          showDeleteDialog={showDeleteDialogMock}
          ariaLabel={"Schema requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers opening a modal with details for the first given schema request", async () => {
      const button = screen.getByRole("button", {
        name: `View schema request for ${schemaRequests[0].topicname}`,
      });

      await userEvent.click(button);

      expect(showDetailsMock).toHaveBeenCalledWith(schemaRequests[0].req_no);
    });

    it("triggers opening a modal with details for the last given schema request", async () => {
      const button = screen.getByRole("button", {
        name: `View schema request for ${
          schemaRequests[schemaRequests.length - 1].topicname
        }`,
      });

      await userEvent.click(button);

      expect(showDetailsMock).toHaveBeenCalledWith(
        schemaRequests[schemaRequests.length - 1].req_no
      );
    });
  });

  describe("triggers opening of a Dialog confirming the deletion when users clicks 'Delete'", () => {
    beforeEach(() => {
      customRender(
        <SchemaRequestTable
          requests={schemaRequests}
          showDetails={showDetailsMock}
          showDeleteDialog={showDeleteDialogMock}
          ariaLabel={"Schema requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers opening a dialog asking for confirmation to delete a schema request", async () => {
      const requestUserCanDelete = schemaRequests[0];
      const button = screen.getByRole("button", {
        name: `Delete schema request for ${requestUserCanDelete.topicname}`,
      });

      await userEvent.click(button);

      expect(showDeleteDialogMock).toHaveBeenCalledWith(
        requestUserCanDelete.req_no
      );
    });

    it("prevents user deleting a request if they are not authorized to do so", async () => {
      const requestUserCanNotDelete = schemaRequests[schemaRequests.length - 1];

      const button = screen.getByRole("button", {
        name: `Delete schema request for ${requestUserCanNotDelete.topicname}`,
      });

      await userEvent.click(button);

      expect(button).toBeDisabled();
      expect(showDeleteDialogMock).not.toHaveBeenCalled;
    });
  });
});
