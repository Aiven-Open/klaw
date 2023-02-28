import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { TopicRequest } from "src/domain/topic";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedSetDetailsModal = jest.fn();
const mockedSetDeclineModal = jest.fn();
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
  },
];

describe("TopicApprovalsTable", () => {
  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Status", relatedField: "requestStatus" },
    { columnHeader: "Claim by team", relatedField: "teamname" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Requested on", relatedField: "requesttimestring" },
    { columnHeader: "", relatedField: null },
    { columnHeader: "", relatedField: null },
    { columnHeader: "", relatedField: null },
  ];

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          setDetailsModal={mockedSetDetailsModal}
          requests={mockedRequests}
          approveIsLoading={false}
          setDeclineModal={mockedSetDeclineModal}
          approveRequest={mockedApproveRequest}
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

    it("shows a Show details button for every row", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /View topic request for /,
      });

      expect(buttons).toHaveLength(mockedRequests.length);
    });

    it("shows an approve button for every row with status CREATED", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Approve topic request for /,
      });
      const createdRequests = mockedRequests.filter(
        ({ requestStatus }) => requestStatus === "CREATED"
      );

      expect(buttons).toHaveLength(createdRequests.length);
    });

    it("shows an decline button for every row with status CREATED", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const buttons = within(table).getAllByRole("button", {
        name: /Decline topic request for /,
      });
      const createdRequests = mockedRequests.filter(
        ({ requestStatus }) => requestStatus === "CREATED"
      );

      expect(buttons).toHaveLength(createdRequests.length);
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          setDetailsModal={mockedSetDetailsModal}
          requests={mockedRequests}
          approveIsLoading={false}
          setDeclineModal={mockedSetDeclineModal}
          approveRequest={mockedApproveRequest}
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
            const content = `${request[column.relatedField]}`;
            const isFormattedTime = column.columnHeader === "Requested on";

            const text = `${content}${isFormattedTime ? " UTC" : ""}`;
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
          setDetailsModal={mockedSetDetailsModal}
          requests={mockedRequests}
          approveIsLoading={false}
          setDeclineModal={mockedSetDeclineModal}
          approveRequest={mockedApproveRequest}
        />
      );
    });
    afterAll(cleanup);

    it("shows a Modal when clicking Show details", async () => {
      const showDetails = screen.getByRole("button", {
        name: "View topic request for test-topic-1",
      });

      await userEvent.click(showDetails);

      expect(mockedSetDetailsModal).toHaveBeenCalledWith({
        isOpen: true,
        topicId: 1000,
      });
    });

    it("shows a Modal when clicking Decline button", async () => {
      const showDetails = screen.getByRole("button", {
        name: "Decline topic request for test-topic-1",
      });

      await userEvent.click(showDetails);

      expect(mockedSetDeclineModal).toHaveBeenCalledWith({
        isOpen: true,
        topicId: 1000,
      });
    });

    it("approves request when clicking Approve button", async () => {
      const showDetails = screen.getByRole("button", {
        name: "Approve topic request for test-topic-1",
      });

      await userEvent.click(showDetails);

      expect(mockedApproveRequest).toHaveBeenCalledWith({
        requestEntityType: "TOPIC",
        reqIds: ["1000"],
      });
    });
  });

  describe("handles action columns loading state", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicApprovalsTable
          setDetailsModal={mockedSetDetailsModal}
          requests={mockedRequests}
          approveIsLoading={true}
          setDeclineModal={mockedSetDeclineModal}
          approveRequest={mockedApproveRequest}
        />
      );
    });
    afterAll(cleanup);

    it("renders disabled decline button", async () => {
      const decline = screen.getByRole("button", {
        name: "Decline topic request for test-topic-1",
      });

      expect(decline).toBeDisabled;
    });
  });
});
