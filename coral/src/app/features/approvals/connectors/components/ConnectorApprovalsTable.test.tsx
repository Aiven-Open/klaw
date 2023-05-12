import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ConnectorApprovalsTable from "src/app/features/approvals/connectors/components/ConnectorApprovalsTable";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import { ConnectorRequest } from "src/domain/connector";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedConnectorRequests: ConnectorRequest[] = [
  {
    environment: "4",
    environmentName: "DEV",
    requestor: "aindriul",
    teamId: 1003,
    teamname: "Ospo",
    requestOperationType: "DELETE",
    requestStatus: "CREATED",
    requesttime: "2023-04-06T08:04:39.783+00:00",
    requesttimestring: "06-Apr-2023 08:04:39",
    currentPage: "1",
    totalNoPages: "1",
    allPageNos: ["1"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,amathieu,roopek,miketest,harshini,mischa,",
    connectorName: "Mirjam-10",
    description: "Mirjam-10",
    connectorConfig:
      '{\n  "name" : "Mirjam-10",\n  "topic" : "testtopic",\n  "tasks.max" : "1",\n  "topics.regex" : "*",\n  "connector.class" : "io.confluent.connect.storage.tools.ConnectorSourceConnector"\n}',
    connectorId: 1026,
    deletable: false,
    editable: false,
  },
  {
    environment: "5",
    environmentName: "TST",
    requestor: "miketest",
    teamId: 1003,
    teamname: "Ospo",
    requestOperationType: "CREATE",
    requestStatus: "APPROVED",
    requesttime: "2023-04-04T13:12:32.970+00:00",
    requesttimestring: "04-Apr-2023 13:12:32",
    currentPage: "1",
    totalNoPages: "1",
    allPageNos: ["1"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,roopek,harshini,mischa,",
    connectorName: "Mirjam-7",
    description: "Mirjam-7",
    connectorConfig:
      '{\n  "name" : "Mirjam-2",\n  "topic" : "testtopic",\n  "tasks.max" : "1",\n  "topics.regex" : "*",\n  "connector.class" : "io.confluent.connect.storage.tools.ConnectorSourceConnector"\n}',
    connectorId: 1010,
    deletable: false,
    editable: false,
  },
];

const createdRequests = mockedConnectorRequests.filter(
  (request) => request.requestStatus === "CREATED"
);

const mockApproveRequest = vi.fn();

describe("ConnectorApprovalsTable", () => {
  beforeEach(mockIntersectionObserver);

  const columnsFieldMap = [
    { columnHeader: "Connector name", relatedField: "connectorName" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Request type", relatedField: "requestOperationType" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Approve", relatedField: null },
    { columnHeader: "Decline", relatedField: null },
  ];

  it("shows a message to user in case there are no requests that match the search criteria", () => {
    render(
      <ConnectorApprovalsTable
        requests={[]}
        onDetails={vi.fn()}
        onApprove={mockApproveRequest}
        onDecline={vi.fn()}
        isBeingApproved={vi.fn()}
        isBeingDeclined={vi.fn()}
        ariaLabel={"Connector approval requests, page 1 of 10"}
      />
    );
    screen.getByText("No Kafka connector requests");
    screen.getByText("No Kafka connector request matched your criteria.");
  });

  describe("user is able to view all the necessary Kafka connector request data and actions", () => {
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={vi.fn()}
          onApprove={vi.fn()}
          onDecline={vi.fn()}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });
    afterEach(cleanup);

    it("shows a table with all Kafka connector requests", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(mockedConnectorRequests.length + 1);
    });

    it("shows an detail button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /View Kafka connector request for /,
      });

      expect(buttons).toHaveLength(mockedConnectorRequests.length);
    });

    it("shows an approve button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve Kafka connector request for /,
      });

      expect(buttons).toHaveLength(mockedConnectorRequests.length);
    });

    it("shows an decline button for every row", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline Kafka connector request for /,
      });

      expect(buttons).toHaveLength(mockedConnectorRequests.length);
    });

    mockedConnectorRequests.forEach((request) => {
      it(`shows a button to show the detailed Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `View Kafka connector request for ${request.connectorName}`,
        });

        expect(button).toBeEnabled();
      });
    });

    createdRequests.forEach((request) => {
      it(`shows a button to approve Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Approve Kafka connector request for ${request.connectorName}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Decline Kafka connector request for ${request.connectorName}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={vi.fn()}
          onApprove={vi.fn()}
          onDecline={vi.fn()}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });

    afterEach(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * mockedConnectorRequests.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      if (column.relatedField) {
        mockedConnectorRequests.forEach((request) => {
          it(`shows field ${column.relatedField} for request number ${request.connectorId}`, () => {
            const table = screen.getByRole("table", {
              name: "Connector approval requests, page 1 of 10",
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
    const onDetails = vi.fn();
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={onDetails}
          onApprove={vi.fn()}
          onDecline={vi.fn()}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });
    afterEach(() => {
      cleanup(), vi.clearAllMocks();
    });
    it("triggers details action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      await userEvent.click(
        within(createdRequestRow).getByRole("button", {
          name: "View Kafka connector request for Mirjam-10",
        })
      );
      expect(onDetails).toHaveBeenCalledTimes(1);
      expect(onDetails).toHaveBeenCalledWith(1026);
    });
  });

  describe("user is able to approve and decline pending requests", () => {
    const onApprove = vi.fn();
    const onDecline = vi.fn();
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={vi.fn()}
          onApprove={onApprove}
          onDecline={onDecline}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });
    afterEach(cleanup);
    it("triggers approve action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve Kafka connector request for Mirjam-10",
      });
      await userEvent.click(approve);
      expect(onApprove).toHaveBeenCalledTimes(1);
      expect(onApprove).toHaveBeenCalledWith(1026);
    });
    it("triggers decline action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline Kafka connector request for Mirjam-10",
      });
      await userEvent.click(decline);
      expect(onDecline).toHaveBeenCalledTimes(1);
      expect(onDecline).toHaveBeenCalledWith(1026);
    });
  });

  describe("user is unable to approve and decline non pending requests", () => {
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={vi.fn()}
          onApprove={vi.fn()}
          onDecline={vi.fn()}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const approve = within(approvedRequestRow).getByRole("button", {
        name: "Approve Kafka connector request for Mirjam-7",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const decline = within(approvedRequestRow).getByRole("button", {
        name: "Decline Kafka connector request for Mirjam-7",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to approve a request if the action is already in progress", () => {
    const isBeingApproved = vi.fn(() => true);
    const isBeingDeclined = vi.fn(() => true);
    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={mockedConnectorRequests}
          onDetails={vi.fn()}
          onApprove={vi.fn()}
          onDecline={vi.fn()}
          isBeingApproved={isBeingApproved}
          isBeingDeclined={isBeingDeclined}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve Kafka connector request for Mirjam-10",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 10",
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline Kafka connector request for Mirjam-10",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to approve or decline a request if table has actions disabled", () => {
    const requestsWithStatusCreated = [
      mockedConnectorRequests[0],
      {
        ...mockedConnectorRequests[1],
        connectorName: "Additional-topic",
        req_no: 1234,
      },
    ];

    beforeEach(() => {
      render(
        <ConnectorApprovalsTable
          requests={requestsWithStatusCreated}
          actionsDisabled={true}
          onDetails={vi.fn()}
          onApprove={mockApproveRequest}
          onDecline={vi.fn()}
          isBeingApproved={vi.fn()}
          isBeingDeclined={vi.fn()}
          ariaLabel={"Connector approval requests, page 1 of 10"}
        />
      );
    });

    afterEach(cleanup);

    requestsWithStatusCreated.forEach((request) => {
      it(`disables button to approve Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Approve Kafka connector request for ${request.connectorName}`,
        });

        expect(button).toBeDisabled();
      });

      it(`disables button to decline Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const button = within(table).getByRole("button", {
          name: `Decline Kafka connector request for ${request.connectorName}`,
        });

        expect(button).toBeDisabled();
      });

      it(`does not disable details for Kafka connector request for topic name ${request.connectorName}`, () => {
        const table = screen.getByRole("table", {
          name: "Connector approval requests, page 1 of 10",
        });
        const detailsButton = within(table).getByRole("button", {
          name: `View Kafka connector request for ${request.connectorName}`,
        });

        expect(detailsButton).toBeEnabled();
      });
    });
  });
});
