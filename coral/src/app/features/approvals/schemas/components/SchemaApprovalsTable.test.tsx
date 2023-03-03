import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { cleanup, render, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { SchemaRequest } from "src/domain/schema-request";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import userEvent from "@testing-library/user-event";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";

const mockedRequests: SchemaRequest[] = [
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
    requestStatus: "CREATED",
    requestOperationType: "CREATE",
    remarks: "asap",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    approvingtime: "2022-11-04T14:54:13.414+00:00",
    totalNoPages: "4",
    allPageNos: ["1"],
    currentPage: "1",
    deletable: false,
    editable: false,
    forceRegister: false,
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
    requestStatus: "APPROVED",
    requestOperationType: "DELETE",
    remarks: "asap",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    approvingtime: "2022-11-04T14:54:13.414+00:00",
    totalNoPages: "4",
    allPageNos: ["1"],
    currentPage: "1",
    deletable: false,
    editable: false,
    forceRegister: false,
  },
];

const createdRequests = mockedRequests.filter(
  (request) => request.requestStatus === "CREATED"
);

const mockSetModals = jest.fn();
const mockApproveRequest = jest.fn();

describe("SchemaApprovalsTable", () => {
  beforeAll(mockIntersectionObserver);

  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Request type", relatedField: "requestOperationType" },
    { columnHeader: "Requested by", relatedField: "username" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Approve", relatedField: null },
    { columnHeader: "Decline", relatedField: null },
  ];

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(
        <SchemaApprovalsTable
          requests={mockedRequests}
          setModals={mockSetModals}
          onApprove={mockApproveRequest}
          quickActionLoading={false}
        />
      );
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

      expect(row).toHaveLength(mockedRequests.length + 1);
    });

    it("shows an detail button for every row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /View schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an approve button for every row where request status is CREATED", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve schema request for /,
      });

      expect(buttons).toHaveLength(createdRequests.length);
    });

    it("shows an decline button for every row where request status is CREATED", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline schema request for /,
      });

      expect(buttons).toHaveLength(createdRequests.length);
    });

    mockedRequests.forEach((request) => {
      it(`shows a button to show the detailed schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });

    createdRequests.forEach((request) => {
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
      render(
        <SchemaApprovalsTable
          requests={mockedRequests}
          setModals={mockSetModals}
          onApprove={mockApproveRequest}
          quickActionLoading={false}
        />
      );
    });

    afterAll(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Schema requests",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * mockedRequests.length
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
        mockedRequests.forEach((request) => {
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

            if (column.columnHeader === "Request type") {
              text = requestOperationTypeNameMap[field as RequestOperationType];
            }
            const cell = within(table).getByRole("cell", { name: text });

            expect(cell).toBeVisible();
          });
        });
      }
    });
  });

  describe("triggers opening of a modal with all details if user clicks button for overview", () => {
    beforeEach(() => {
      render(
        <SchemaApprovalsTable
          requests={mockedRequests}
          setModals={mockSetModals}
          onApprove={mockApproveRequest}
          quickActionLoading={false}
        />
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers opening a modal with details for the first given schema request", async () => {
      const button = screen.getByRole("button", {
        name: `View schema request for ${mockedRequests[0].topicname}`,
      });

      await userEvent.click(button);

      expect(mockSetModals).toHaveBeenCalledWith({
        open: "DETAILS",
        req_no: mockedRequests[0].req_no,
      });
    });

    it("triggers opening a modal with details for the laft given schema request", async () => {
      const button = screen.getByRole("button", {
        name: `View schema request for ${
          mockedRequests[mockedRequests.length - 1].topicname
        }`,
      });

      await userEvent.click(button);

      expect(mockSetModals).toHaveBeenCalledWith({
        open: "DETAILS",
        req_no: mockedRequests[mockedRequests.length - 1].req_no,
      });
    });
  });

  describe("disables the quick actions dependent on props", () => {
    const requestsWithStatusCreated = [
      mockedRequests[0],
      { ...mockedRequests[0], topicname: "Additional-topic", req_no: 1234 },
    ];

    beforeAll(() => {
      render(
        <SchemaApprovalsTable
          requests={requestsWithStatusCreated}
          setModals={mockSetModals}
          onApprove={mockApproveRequest}
          quickActionLoading={true}
        />
      );
    });

    afterAll(cleanup);

    requestsWithStatusCreated.forEach((request) => {
      it(`disables button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `Approve schema request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`disables button to decline schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const button = within(table).getByRole("button", {
          name: `Decline schema request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`does not disable details for schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Schema requests" });
        const detailsButton = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(detailsButton).toBeEnabled();
      });
    });
  });

  describe("triggers approval of a request when user clicks button", () => {
    const testRequest = mockedRequests[0];
    beforeEach(() => {
      render(
        <SchemaApprovalsTable
          requests={mockedRequests}
          setModals={mockSetModals}
          onApprove={mockApproveRequest}
          quickActionLoading={false}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers the approval of a request when user clicks button", async () => {
      const button = screen.getByRole("button", {
        name: `Approve schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(button);

      expect(mockApproveRequest).toHaveBeenCalledWith(testRequest.req_no);
    });
  });
});
