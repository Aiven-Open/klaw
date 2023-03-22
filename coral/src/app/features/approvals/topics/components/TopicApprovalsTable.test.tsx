import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { TopicRequest } from "src/domain/topic";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";

const mockedApproveRequest = jest.fn();

const mockedRequests: TopicRequest[] = [
  {
    topicname: "test-topic-1",
    environment: "1",
    topicpartitions: 4,
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "BRG",
    topicid: 1000,
    advancedTopicConfigEntries: [
      {
        configKey: "cleanup.policy",
        configValue: "delete",
      },
    ],
    requestOperationType: "CREATE",
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    requestStatus: "CREATED",
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
    editable: true,
    deletable: true,
    deleteAssociatedSchema: false,
  },
  {
    topicname: "test-topic-2",
    environment: "1",
    topicpartitions: 4,
    teamname: "MIRRORUNIVERSE",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "SBY",
    topicid: 1001,
    advancedTopicConfigEntries: [
      {
        configKey: "compression.type",
        configValue: "snappy",
      },
    ],

    requestOperationType: "UPDATE",
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    requestStatus: "APPROVED",
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
    editable: true,
    deletable: true,
    deleteAssociatedSchema: false,
  },
];

describe("TopicApprovalsTable", () => {
  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Request type", relatedField: "requestOperationType" },
    { columnHeader: "Team", relatedField: "teamname" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "", relatedField: null },
    { columnHeader: "", relatedField: null },
    { columnHeader: "", relatedField: null },
  ];

  describe("empty state is handled", () => {
    beforeEach(() => {
      mockIntersectionObserver();
    });

    afterEach(cleanup);

    it("shows a message to user in case there are no requests that match the search criteria", () => {
      render(
        <TopicApprovalsTable
          requests={[]}
          onDetails={jest.fn()}
          onApprove={mockedApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
      screen.getByText("No Topic requests");
      screen.getByText("No Topic request matched your criteria.");
    });
  });

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={mockedApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });
    afterAll(cleanup);

    it("shows a table with all topic requests", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });

      expect(table).toBeVisible();
    });

    it("shows all column headers", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const header = within(table).getAllByRole("columnheader");

      expect(header).toHaveLength(columnsFieldMap.length);
    });

    it("shows a row for each given requests plus header row", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const row = within(table).getAllByRole("row");

      expect(row).toHaveLength(mockedRequests.length + 1);
    });

    it("shows an detail button for every row", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /View topic request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an approve button for every row", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve topic request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an decline button for every row", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline topic request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={mockedApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });

    afterAll(cleanup);

    it(`renders the right amount of cells`, () => {
      const table = screen.getByRole("table", {
        name: "Topic requests",
      });
      const cells = within(table).getAllByRole("cell");

      expect(cells).toHaveLength(
        columnsFieldMap.length * mockedRequests.length
      );
    });

    columnsFieldMap.forEach((column) => {
      it(`shows a column header for ${column.columnHeader}`, () => {
        if (column.relatedField === null) {
          return;
        }
        const table = screen.getByRole("table", {
          name: "Topic requests",
        });
        const header = within(table).getByRole("columnheader", {
          name: column.columnHeader,
        });

        expect(header).toBeVisible();
      });

      if (column.relatedField) {
        mockedRequests.forEach((request) => {
          it(`shows field ${column.relatedField} for topic id ${request.topicid}`, () => {
            const table = screen.getByRole("table", {
              name: "Topic requests",
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

  describe("handles interaction with action columns", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={mockedApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });
    afterAll(cleanup);

    it("approves request when clicking Approve button", async () => {
      const showDetails = screen.getByRole("button", {
        name: "Approve topic request for test-topic-1",
      });

      await userEvent.click(showDetails);

      expect(mockedApproveRequest).toHaveBeenCalledWith(1000);
    });
  });

  describe("disables the quick actions dependent on props", () => {
    const requestsWithStatusCreated = [
      mockedRequests[0],
      { ...mockedRequests[0], topicname: "Additional-topic", topicid: 1234 },
    ];

    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          requests={requestsWithStatusCreated}
          actionsDisabled
          onDetails={jest.fn()}
          onApprove={mockedApproveRequest}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });
    afterAll(cleanup);

    requestsWithStatusCreated.forEach((request) => {
      it(`disables button to approve topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const button = within(table).getByRole("button", {
          name: `Approve topic request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`disables  button to decline topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const button = within(table).getByRole("button", {
          name: `Decline topic request for ${request.topicname}`,
        });

        expect(button).toBeDisabled();
      });

      it(`does not disables details for topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const detailsButton = within(table).getByRole("button", {
          name: `View topic request for ${request.topicname}`,
        });

        expect(detailsButton).toBeEnabled();
      });
    });
  });

  describe("user is unable to approve and decline non pending requests", () => {
    beforeEach(() => {
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is not in created state", async () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const approve = within(approvedRequestRow).getByRole("button", {
        name: "Approve topic request for test-topic-2",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is not in created state", async () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const decline = within(approvedRequestRow).getByRole("button", {
        name: "Decline topic request for test-topic-2",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to trigger action if some action is already in progress", () => {
    const isBeingApproved = jest.fn(() => true);
    const isBeingDeclined = jest.fn(() => true);
    beforeEach(() => {
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={jest.fn()}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={isBeingApproved}
          isBeingDeclined={isBeingDeclined}
        />
      );
    });
    afterEach(cleanup);
    it("disables approve action if request is already in progress", async () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve topic request for test-topic-1",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is already in progress", async () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline topic request for test-topic-1",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is able to view request details", () => {
    const onDetails = jest.fn();
    beforeEach(() => {
      render(
        <TopicApprovalsTable
          requests={mockedRequests}
          onDetails={onDetails}
          onApprove={jest.fn()}
          onDecline={jest.fn()}
          isBeingApproved={jest.fn()}
          isBeingDeclined={jest.fn()}
        />
      );
    });
    afterAll(cleanup);
    it("triggers details action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      await userEvent.click(
        within(createdRequestRow).getByRole("button", {
          name: "View topic request for test-topic-1",
        })
      );
      expect(onDetails).toHaveBeenCalledTimes(1);
      expect(onDetails).toHaveBeenCalledWith(1000);
    });
  });
});
