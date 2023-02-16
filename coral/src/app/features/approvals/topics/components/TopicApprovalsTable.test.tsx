import { cleanup, render, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { TopicRequestStatus, TopicRequestTypes } from "src/domain/topic";

const mockedRequests = [
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
    topictype: "Create" as TopicRequestTypes,
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    topicstatus: "created" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
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

    topictype: "Update" as TopicRequestTypes,
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    topicstatus: "approved" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
];

describe("TopicApprovalsTable", () => {
  const columnsFieldMap = [
    { columnHeader: "Topic", relatedField: "topicname" },
    { columnHeader: "Environment", relatedField: "environmentName" },
    { columnHeader: "Type", relatedField: "topictype" },
    { columnHeader: "Claim by team", relatedField: "teamname" },
    { columnHeader: "Requested by", relatedField: "requestor" },
    { columnHeader: "Date requested", relatedField: "requesttimestring" },
    { columnHeader: "Details", relatedField: null },
    { columnHeader: "Approve", relatedField: null },
    { columnHeader: "Decline", relatedField: null },
  ];

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<TopicApprovalsTable requests={mockedRequests} />);
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

    mockedRequests.forEach((request) => {
      it(`shows a button to show the detailed topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const button = within(table).getByRole("button", {
          name: `View topic request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const button = within(table).getByRole("button", {
          name: `Approve topic request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });

      it(`shows a button to approve topic request for topic name ${request.topicname}`, () => {
        const table = screen.getByRole("table", { name: "Topic requests" });
        const button = within(table).getByRole("button", {
          name: `Decline topic request for ${request.topicname}`,
        });

        expect(button).toBeEnabled();
      });
    });
  });

  describe("renders all content based on the column definition", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<TopicApprovalsTable requests={mockedRequests} />);
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
            const isFormattedTime = column.columnHeader === "Date requested";

            const text = `${content}${isFormattedTime ? " UTC" : ""}`;
            const cell = within(table).getByRole("cell", { name: text });

            expect(cell).toBeVisible();
          });
        });
      }
    });
  });
});
