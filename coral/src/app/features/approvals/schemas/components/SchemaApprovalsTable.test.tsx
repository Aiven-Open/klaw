import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { cleanup, render, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { columns } from "src/app/features/approvals/schemas/components/schema-request-table";

const mockedRequests = [
  {
    req_no: 1014,
    topicname: "testtopic-first",
    environment: "1",
    environmentName: "BRG",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    req_no: 1013,
    topicname: "testtopic-second",
    environment: "2",
    environmentName: "SEC",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
];

describe("SchemaApprovalsTable", () => {
  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<SchemaApprovalsTable requests={mockedRequests} />);
    });
    afterAll(cleanup);

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columns.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(mockedRequests.length + 1);
    });

    it("shows an detail button for every row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /View schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an approve button for every row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an decline button for every row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    mockedRequests.forEach((request) => {
      it(`shows a button to show the detailed schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `Approve schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `Decline schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<SchemaApprovalsTable requests={mockedRequests} />);
    });

    afterAll(cleanup);

    columns.forEach((col) => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      const colRequestFieldReference: string = col.field;
      const columnHeader = col.headerName;

      it(`shows a column header for ${columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests",
        });
        const header = within(table).getByRole("columnheader", {
          name: columnHeader,
        });

        expect(header).toBeVisible();
      });

      it(`renders the right amount of cells`, () => {
        const table = screen.getByRole("table", {
          name: "Schema requests",
        });
        const cells = within(table).getAllByRole("cell");

        expect(cells).toHaveLength(mockedRequests.length * columns.length);
      });

      if (colRequestFieldReference) {
        mockedRequests.forEach((request) => {
          it(`shows field ${colRequestFieldReference} for request number ${request.req_no}`, () => {
            const table = screen.getByRole("table", {
              name: "Schema requests",
            });

            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            //@ts-ignore
            const content = `${request[colRequestFieldReference]}`;
            const isFormattedTime =
              colRequestFieldReference === "requesttimestring";

            const text = `${content}${isFormattedTime ? " UTC" : ""}`;
            const cell = within(table).getByText(text);

            expect(cell).toBeVisible();
          });
        });
      }
    });
  });
});
