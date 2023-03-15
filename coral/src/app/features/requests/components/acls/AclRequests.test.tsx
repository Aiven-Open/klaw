import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AclRequests } from "src/app/features/requests/components/acls/AclRequests";
import { getAclRequests } from "src/domain/acl/acl-api";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api.ts");

const mockGetAclRequests = getAclRequests as jest.MockedFunction<
  typeof getAclRequests
>;

const mockGetAclRequestsResponse = transformAclRequestApiResponse([
  {
    remarks: undefined,
    consumergroup: "-na-",
    acl_ip: undefined,
    acl_ssl: ["josepprat"],
    aclPatternType: "LITERAL",
    transactionalId: undefined,
    req_no: 1220,
    topicname: "uptimetopic",
    environment: "1",
    teamname: "Ospo",
    aclType: "PRODUCER",
    aclIpPrincipleType: "PRINCIPAL",
    requestStatus: "CREATED",
    requestOperationType: "CREATE",
    environmentName: "DEV",
    teamId: 1003,
    requestingteam: 1003,
    requestingTeamName: "Ospo",
    appname: "App",
    username: "josepprat",
    requesttime: "2023-03-10T12:08:46.040+00:00",
    requesttimestring: "10-Mar-2023 12:08:46",
    approver: undefined,
    approvingtime: undefined,
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "22",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,calummuir,roopek,MikeTest,Mischa,",
    deletable: false,
    editable: false,
  },
  {
    remarks: undefined,
    consumergroup: "sdsdsds",
    acl_ip: ["1.1.1.1", "2.2.2.2"],
    acl_ssl: ["User:*"],
    aclPatternType: "LITERAL",
    transactionalId: undefined,
    req_no: 1217,
    topicname: "newaudittopic",
    environment: "2",
    teamname: "Ospo",
    aclType: "CONSUMER",
    aclIpPrincipleType: "IP_ADDRESS",
    requestStatus: "DECLINED",
    requestOperationType: "CREATE",
    environmentName: "TST",
    teamId: 1003,
    requestingteam: 1003,
    requestingTeamName: "Ospo",
    appname: "App",
    username: "josepprat",
    requesttime: "2023-03-03T07:18:23.687+00:00",
    requesttimestring: "03-Mar-2023 07:18:23",
    approver: "amathieu",
    approvingtime: "2023-03-10T09:12:21.328+00:00",
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "22",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,calummuir,roopek,MikeTest,Mischa,",
    deletable: true,
    editable: true,
  },
]);

describe("AclRequests", () => {
  beforeEach(() => {
    mockIntersectionObserver();
    mockGetAclRequests.mockResolvedValue(mockGetAclRequestsResponse);
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("makes a request to the api to get the team's ACL requests", () => {
    customRender(<AclRequests />, {
      queryClient: true,
      memoryRouter: true,
    });
    expect(getAclRequests).toBeCalledTimes(1);
  });

  describe("renders pagination dependent on response", () => {
    beforeEach(() => {
      mockGetAclRequests.mockResolvedValue({
        totalPages: 1,
        currentPage: 1,
        entries: [],
      });
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = "/?page=100";
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetAclRequests).toHaveBeenCalledWith({
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetAclRequests).toHaveBeenCalledWith({
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetAclRequests.mockResolvedValue({
        ...mockGetAclRequestsResponse,
        totalPages: 1,
      });

      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });
      expect(pagination).not.toBeInTheDocument();
    });

    it("shows a pagination when response has more then one page", async () => {
      mockGetAclRequests.mockResolvedValue({
        totalPages: 2,
        currentPage: 1,
        entries: [],
      });

      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 2",
      });
      expect(pagination).toBeVisible();
    });

    it("shows the currently active page based on api response", async () => {
      mockGetAclRequests.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 4",
      });
      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetAclRequests.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 1 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 1 of 3"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 2",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetAclRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "2",
      });
    });
  });
});
