import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicSubscriptionsTable } from "src/app/features/topics/details/subscriptions/TopicSubscriptionsTable";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockUserSubs: AclOverviewInfo[] = [
  {
    req_no: "1006",
    acl_ssl: "aivtopic3user",
    topicname: "aivtopic3",
    topictype: "Producer",
    consumergroup: "-na-",
    environment: "1",
    environmentName: "DEV",
    teamname: "Ospo",
    teamid: 0,
    aclPatternType: "LITERAL",
    showDeleteAcl: true,
    kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
  },
  {
    req_no: "1011",
    acl_ssl: "declineme",
    topicname: "aivtopic3",
    topictype: "Producer",
    consumergroup: "-na-",
    environment: "1",
    environmentName: "DEV",
    teamname: "Ospo",
    teamid: 0,
    aclPatternType: "LITERAL",
    showDeleteAcl: true,
    kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
  },
  {
    req_no: "1060",
    acl_ssl: "amathieu",
    topicname: "aivtopic3",
    topictype: "Producer",
    consumergroup: "-na-",
    environment: "1",
    environmentName: "DEV",
    teamname: "Ospo",
    teamid: 0,
    aclPatternType: "LITERAL",
    showDeleteAcl: true,
    kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
  },
];

const mockedPrefixedSubs: AclOverviewInfo[] = [
  {
    req_no: "1063",
    aclPatternType: "PREFIXED",
    environment: "2",
    environmentName: "TST",
    kafkaFlavorType: "APACHE_KAFKA",
    showDeleteAcl: true,
    teamid: 1003,
    teamname: "Ospo",
    topicname: "aivendemot",
    topictype: "Producer",
    acl_ssl: "CN=myhosttest,OU=IS,OU=OU,OU=Services,O=Org",
  },
];

const mockedTransactionalSubs: AclOverviewInfo[] = [
  {
    req_no: "1064",
    aclPatternType: "LITERAL",
    environment: "2",
    environmentName: "TST",
    kafkaFlavorType: "APACHE_KAFKA",
    showDeleteAcl: true,
    teamid: 1003,
    teamname: "Ospo",
    topicname: "aivendemotopic",
    topictype: "Producer",
    acl_ip: "32.121.67.2",
    acl_ssl: "User:*",
    transactionalId: "tsttxnid",
  },
];

const userSubsColumnNames = [
  "Principals/Usernames",
  "IP addresses",
  "ACL type",
  "Team",
  "Details",
  "Delete",
];

const prefixedSubsColumnNames = [
  "Prefix",
  "Principals/Usernames",
  "IP addresses",
  "ACL type",
  "Team",
  "Details",
  "Delete",
];
const transactionalSubsColumnNames = [
  "Transactional ID",
  "Principals/Usernames",
  "IP addresses",
  "ACL type",
  "Team",
  "Details",
  "Delete",
];

const mockOnDelete = jest.fn();
const mockOnDetails = jest.fn();

describe("TopicSubscriptionsTable.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  afterAll(() => {
    cleanup();
  });

  describe("should render empty state when no data is passed", () => {
    it("renders empty state when passed empty data", () => {
      render(
        <TopicSubscriptionsTable
          isUpdating={false}
          onDelete={mockOnDelete}
          onDetails={mockOnDetails}
          filteredData={[]}
          selectedSubs="aclInfoList"
        />
      );
      const emptyStateMessage = screen.getByText(
        "No subscription matched your criteria."
      );
      expect(emptyStateMessage).toBeVisible();
    });
  });

  describe("should render correct columns and rows according to the type of subscription passed", () => {
    describe("renders correct data for User subscriptions", () => {
      beforeAll(() => {
        render(
          <TopicSubscriptionsTable
            isUpdating={false}
            onDelete={mockOnDelete}
            onDetails={mockOnDetails}
            filteredData={mockUserSubs}
            selectedSubs="aclInfoList"
          />
        );
      });

      afterAll(cleanup);

      it("renders correct columns", () => {
        const columns = userSubsColumnNames.map((name) =>
          screen.getByRole("columnheader", { name })
        );
        expect(columns).toHaveLength(userSubsColumnNames.length);
      });

      it("renders all rows", () => {
        const rows = screen.getAllByRole("row");

        expect(rows).toHaveLength(mockUserSubs.length + 1);
      });

      it("renders the filtered data as row content", () => {
        const firstRow = mockUserSubs[0];
        const rowContent = `${
          firstRow.acl_ssl
        } * ${firstRow.topictype.toUpperCase()} ${
          firstRow.teamname
        } Details Delete`;

        const firstContentRow = screen.getByRole("row", { name: rowContent });

        expect(firstContentRow).toBeVisible();
      });
    });

    describe("renders correct data for Prefixed subscriptions", () => {
      beforeAll(() => {
        render(
          <TopicSubscriptionsTable
            isUpdating={false}
            onDelete={mockOnDelete}
            onDetails={mockOnDetails}
            filteredData={mockedPrefixedSubs}
            selectedSubs="prefixedAclInfoList"
          />
        );
      });
      afterAll(cleanup);

      it("renders correct columns", () => {
        const columns = prefixedSubsColumnNames.map((name) =>
          screen.getByRole("columnheader", { name })
        );
        expect(columns).toHaveLength(prefixedSubsColumnNames.length);
      });

      it("renders all rows", () => {
        const rows = screen.getAllByRole("row");

        expect(rows).toHaveLength(mockedPrefixedSubs.length + 1);
      });

      it("renders the filtered data as row content", () => {
        const firstRow = mockedPrefixedSubs[0];
        const rowContent = `${firstRow.topicname} ${
          firstRow.acl_ssl
        } * ${firstRow.topictype.toUpperCase()} ${
          firstRow.teamname
        } Details Delete`;

        const firstContentRow = screen.getByRole("row", { name: rowContent });

        expect(firstContentRow).toBeVisible();
      });
    });

    describe("renders correct data for Transactional subscriptions", () => {
      beforeAll(() => {
        render(
          <TopicSubscriptionsTable
            isUpdating={false}
            onDelete={mockOnDelete}
            onDetails={mockOnDetails}
            filteredData={mockedTransactionalSubs}
            selectedSubs="transactionalAclInfoList"
          />
        );
      });
      afterAll(cleanup);

      it("renders correct columns", () => {
        const columns = transactionalSubsColumnNames.map((name) =>
          screen.getByRole("columnheader", { name })
        );
        expect(columns).toHaveLength(transactionalSubsColumnNames.length);
      });

      it("renders all rows", () => {
        const rows = screen.getAllByRole("row");

        expect(rows).toHaveLength(mockedTransactionalSubs.length + 1);
      });

      it("renders the filtered data as row content", () => {
        const firstRow = mockedTransactionalSubs[0];
        const rowContent = `${firstRow.transactionalId} ${firstRow.acl_ssl} ${
          firstRow.acl_ip
        } ${firstRow.topictype.toUpperCase()} ${
          firstRow.teamname
        } Details Delete`;

        const firstContentRow = screen.getByRole("row", { name: rowContent });

        expect(firstContentRow).toBeVisible();
      });
    });

    describe("renders loading information when existing data is being updated", () => {
      beforeAll(() => {
        render(
          <TopicSubscriptionsTable
            isUpdating={true}
            onDelete={mockOnDelete}
            onDetails={mockOnDetails}
            filteredData={mockUserSubs}
            selectedSubs="aclInfoList"
          />
        );
      });

      afterAll(cleanup);

      it("renders correct columns for User subscriptions", () => {
        const columns = userSubsColumnNames.map((name) =>
          screen.getByRole("columnheader", { name })
        );

        expect(columns).toHaveLength(userSubsColumnNames.length);
      });

      it("renders all rows", () => {
        const rows = screen.getAllByRole("row");

        expect(rows).toHaveLength(mockUserSubs.length + 1);
      });

      it("renders no content in rows other then header row", () => {
        const rows = screen.getAllByRole("row");

        expect(rows[1]).toHaveTextContent("");
        expect(rows[2]).toHaveTextContent("");
        expect(rows[3]).toHaveTextContent("");
      });
    });

    describe("renders loading information when former empty data is being updated", () => {
      beforeAll(() => {
        render(
          <TopicSubscriptionsTable
            isUpdating={true}
            onDelete={mockOnDelete}
            onDetails={mockOnDetails}
            filteredData={[]}
            selectedSubs="aclInfoList"
          />
        );
      });

      afterAll(cleanup);

      it("renders correct columns for User subscriptions", () => {
        const columns = userSubsColumnNames.map((name) =>
          screen.getByRole("columnheader", { name })
        );

        expect(columns).toHaveLength(userSubsColumnNames.length);
      });

      it("renders header and a empty loading rows", () => {
        const rows = screen.getAllByRole("row");

        expect(rows).toHaveLength(2);
      });

      it("renders no content in rows other then header row", () => {
        const rows = screen.getAllByRole("row");

        expect(rows[1]).toHaveTextContent("");
      });
    });
  });

  describe("should call correct fns when clicking action buttons", () => {
    beforeEach(() =>
      render(
        <TopicSubscriptionsTable
          isUpdating={false}
          onDelete={mockOnDelete}
          onDetails={mockOnDetails}
          filteredData={mockUserSubs}
          selectedSubs="aclInfoList"
        />
      )
    );
    afterEach(cleanup);

    it("calls onDelete when clicking Delete button", async () => {
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Create deletion request for request 1006",
      });

      await userEvent.click(button);

      expect(mockOnDelete).toHaveBeenCalledWith("1006");
    });

    it("calls onDetails when clicking Details button", async () => {
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Show details of request 1006",
      });

      await userEvent.click(button);

      expect(mockOnDetails).toHaveBeenCalledWith({
        consumerGroupId: "-na-",
        aclReqNo: "1006",
        isAivenCluster: true,
      });
    });
  });
});
