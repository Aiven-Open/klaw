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
  "Requested on",
  "Delete",
];

const prefixedSubsColumnNames = [
  "Prefix",
  "Principals/Usernames",
  "IP addresses",
  "ACL type",
  "Team",
  "Requested on",
  "Delete",
];
const transactionalSubsColumnNames = [
  "Transactional ID",
  "Principals/Usernames",
  "IP addresses",
  "ACL type",
  "Team",
  "Requested on",
  "Delete",
];

const mockOnDelete = jest.fn();

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
          onDelete={mockOnDelete}
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

  describe("should render correct columns according to the type of subscription passed", () => {
    afterEach(cleanup);
    it("renders correct columns for User subscriptions", () => {
      render(
        <TopicSubscriptionsTable
          onDelete={mockOnDelete}
          filteredData={mockUserSubs}
          selectedSubs="aclInfoList"
        />
      );
      const columns = userSubsColumnNames.map((name) =>
        screen.getByRole("columnheader", { name })
      );
      expect(columns).toHaveLength(userSubsColumnNames.length);
    });

    it("renders correct columns for Prefixed subscriptions", () => {
      render(
        <TopicSubscriptionsTable
          onDelete={mockOnDelete}
          filteredData={mockedPrefixedSubs}
          selectedSubs="prefixedAclInfoList"
        />
      );
      const columns = prefixedSubsColumnNames.map((name) =>
        screen.getByRole("columnheader", { name })
      );
      expect(columns).toHaveLength(prefixedSubsColumnNames.length);
    });

    it("renders correct columns for Transactional subscriptions", () => {
      render(
        <TopicSubscriptionsTable
          onDelete={mockOnDelete}
          filteredData={mockedTransactionalSubs}
          selectedSubs="transactionalAclInfoList"
        />
      );

      const columns = transactionalSubsColumnNames.map((name) =>
        screen.getByRole("columnheader", { name })
      );
      expect(columns).toHaveLength(transactionalSubsColumnNames.length);
    });
  });

  describe("should call onDelete when clicking Delete button", () => {
    afterEach(cleanup);
    it("calls onDelete when clicking Delete button", async () => {
      render(
        <TopicSubscriptionsTable
          onDelete={mockOnDelete}
          filteredData={mockUserSubs}
          selectedSubs="aclInfoList"
        />
      );
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Create deletion request for request 1006",
      });

      await userEvent.click(button);

      expect(mockOnDelete).toHaveBeenCalledWith("1006");
    });
  });
});
