import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { cleanup, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { SchemaRequest } from "src/domain/schema-request";
import { userEvent } from "@testing-library/user-event";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";
import { RequestOperationType, RequestStatus } from "src/domain/requests";
import { customRender } from "src/services/test-utils/render-with-wrappers";

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
    requestor: "jlpicard",
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
    schemaType: "JSON",
  },
  {
    req_no: 1013,
    topicname: "testtopic-second",
    environment: "2",
    environmentName: "SEC",
    schemaversion: "1.0",
    teamname: "NCC1702D",
    teamId: 1702,
    appname: "App",
    schemafull: "",
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    requestStatus: "APPROVED",
    requestOperationType: "DELETE",
    remarks: "asap",
    approvingTeamDetails:
      "Team : NCC1702D, Users : jlpicard, worf, bcrusher, geordilf",
    approvingtime: "2022-11-04T14:54:13.414+00:00",
    totalNoPages: "4",
    allPageNos: ["1"],
    currentPage: "1",
    deletable: false,
    editable: false,
    forceRegister: true,
    schemaType: "JSON",
  },
];

const createdRequests = mockedRequests.filter(
  (request) => request.requestStatus === "CREATED"
);

const mockApproveRequest = jest.fn();

describe("SchemaApprovalsTable", () => {
  beforeAll(mockIntersectionObserver);

  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Team", relatedField: "teamname" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Request type", relatedField: "requestOperationType" },
    { columnHeader: "Additional notes", relatedField: "forceRegister" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Approve", relatedField: null },
    { columnHeader: "Decline", relatedField: null },
  ];

  it("shows a message to user in case there are no requests that match the search criteria", () => {
    customRender(
      <SchemaApprovalsTable
        requests={[]}
        onDetails={jest.fn()}
        onApprove={mockApproveRequest}
        onDecline={jest.fn()}
        isBeingApproved={jest.fn()}
        isBeingDeclined={jest.fn()}
        ariaLabel={"Schema approval requests, page 1 of 10"}
      />,
      { memoryRouter: true }
    );
    screen.getByText("No Schema requests");
    screen.getByText("No Schema request matched your criteria.");
  });

  describe("user is able to view all the necessary schema request data and actions", () => {
    beforeAll(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterAll(cleanup);

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(mockedRequests.length + 1);
    });

    it("shows an detail button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /View schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an approve button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an decline button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline schema request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    mockedRequests.forEach((request) => {
      it(`shows a button to show the detailed schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });

    createdRequests.forEach((request) => {
      it(`shows a button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Approve schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Decline schema request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * mockedRequests.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      if (column.relatedField) {
        mockedRequests.forEach((request) => {
          const isTeamLink = column.columnHeader === "Team";
          const isUserLink = column.columnHeader === "Requested by";
          const isFormattedDate = column.columnHeader === "Requested on";
          const isStatus = column.columnHeader === "Status";
          const isRequestType = column.columnHeader === "Request type";
          const isAdditionalNotes = column.columnHeader === "Additional notes";

          it(`shows field ${column.relatedField} for request number ${request.req_no}`, () => {
            const table = screen.getByRole("table", {
              name: "Schema approval requests, page 1 of 10",
            });

            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            //@ts-ignore
            const field = request[column.relatedField];

            if (isTeamLink) {
              const cell = within(table).getByRole("cell", { name: field });
              const link = within(cell).getByRole("link", { name: field });

              expect(link).toBeVisible();
              expect(link).toHaveAttribute("href", "/configuration/teams");

              // formatting for readability
            } else if (isUserLink) {
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
            } else if (isAdditionalNotes) {
              const cell = within(table).getByRole("cell", {
                name: field ? "Force register applied" : "",
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

  describe("triggers opening of a modal with all details if user clicks button for overview", () => {
    const onDetails = jest.fn();
    beforeEach(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={onDetails}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });
    it("triggers details action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      await userEvent.click(
        within(createdRequestRow).getByRole("button", {
          name: "View schema request for testtopic-first",
        })
      );
      expect(onDetails).toHaveBeenCalledTimes(1);
      expect(onDetails).toHaveBeenCalledWith(1014);
    });
  });

  describe("user is able to approve and decline pending requests", () => {
    const onApprove = jest.fn();
    const onDecline = jest.fn();
    beforeEach(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={onApprove}
          onDecline={onDecline}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterEach(cleanup);
    it("triggers approve action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve schema request for testtopic-first",
      });
      await userEvent.click(approve);
      expect(onApprove).toHaveBeenCalledTimes(1);
      expect(onApprove).toHaveBeenCalledWith(1014);
    });
    it("triggers decline action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline schema request for testtopic-first",
      });
      await userEvent.click(decline);
      expect(onDecline).toHaveBeenCalledTimes(1);
      expect(onDecline).toHaveBeenCalledWith(1014);
    });
  });

  describe("user is unable to approve and decline non pending requests", () => {
    beforeEach(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const approve = within(approvedRequestRow).getByRole("button", {
        name: "Approve schema request for testtopic-second",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const decline = within(approvedRequestRow).getByRole("button", {
        name: "Decline schema request for testtopic-second",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to approve a request if the action is already in progress", () => {
    const isBeingApproved = jest.fn(() => true);
    const isBeingDeclined = jest.fn(() => true);
    beforeEach(() => {
      customRender(
        <SchemaApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={isBeingApproved}
          isBeingDeclined={isBeingDeclined}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve schema request for testtopic-first",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: "Schema approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline schema request for testtopic-first",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to approve or decline a request if table has actions disabled", () => {
    const requestsWithStatusCreated = [
      mockedRequests[0],
      { ...mockedRequests[0], topicname: "Additional-topic", req_no: 1234 },
    ];

    beforeAll(() => {
      customRender(
        <SchemaApprovalsTable
          requests={requestsWithStatusCreated}
          actionsDisabled={true}
          onDetails={jest.fn()}
          onApprove={mockApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
          ariaLabel={"Schema approval requests, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(cleanup);

    requestsWithStatusCreated.forEach((request) => {
      it(`disables button to approve schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Approve schema request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`disables button to decline schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Decline schema request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`does not disable details for schema request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", {
          name: "Schema approval requests, page 1 of 10",
        });
        const detailsButton = within(table).getByRole("button", {
          name: `View schema request for ${request.topicname}`,
        });

        expect(detailsButton).toBeEnabled();
      });
    });
  });
});
