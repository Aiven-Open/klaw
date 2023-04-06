import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AclRequests } from "src/app/features/requests/acls/AclRequests";
import { deleteAclRequest, getAclRequests } from "src/domain/acl/acl-api";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import { getEnvironments } from "src/domain/environment";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetAclRequests = getAclRequests as jest.MockedFunction<
  typeof getAclRequests
>;
const mockDeleteAclRequests = deleteAclRequest as jest.MockedFunction<
  typeof deleteAclRequest
>;
const mockGetEnvironments = getEnvironments as jest.MockedFunction<
  typeof getEnvironments
>;

const mockGetAclRequestsResponse = transformAclRequestApiResponse([
  {
    remarks: undefined,
    consumergroup: "-na-",
    acl_ip: [],
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
    requestor: "josepprat",
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
    requestor: "josepprat",
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

const mockGetEnvironmentsResponse = [
  createEnvironment({
    name: "DEV",
    id: "1",
  }),
];

describe("AclRequests", () => {
  beforeEach(() => {
    mockGetAclRequests.mockResolvedValue(mockGetAclRequestsResponse);
    mockGetEnvironments.mockResolvedValue(mockGetEnvironmentsResponse);
    mockIntersectionObserver();
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
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
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
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
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
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
      });
    });
  });

  describe("user can filter ACL requests based on the topic name", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?topic=abc",
      });
      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "abc",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
      });
    });

    it("enables user to filter ACL requests by Topic name", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });
      const search = screen.getByRole("search");
      expect(search).toBeEnabled();
      expect(search).toHaveAccessibleDescription(
        'Search for an partial match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
      await userEvent.type(search, "abc");
      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "abc",
          env: "ALL",
          aclType: "ALL",
          requestStatus: "ALL",
          operationType: undefined,
          isMyRequest: false,
        });
      });
    });
  });

  describe("user can filter ACL requests based on the environment", () => {
    afterEach(() => {
      cleanup();
    });

    it("populates the filter from the url search parameters", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?environment=1",
      });

      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      expect(envFilter).toHaveDisplayValue("DEV");

      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "1",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
      });
    });

    it("enables user to filter ACL requests by Environment", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });
      expect(envFilter).toBeEnabled();

      await userEvent.selectOptions(envFilter, "DEV");
      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "",
          env: "1",
          aclType: "ALL",
          requestStatus: "ALL",
          operationType: undefined,
          isMyRequest: false,
        });
      });
    });
  });

  describe("user can filter ACL requests based on the ACL type", () => {
    afterEach(() => {
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?aclType=CONSUMER",
      });

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by ACL type",
      });

      expect(envFilter).toHaveDisplayValue("CONSUMER");

      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "ALL",
        aclType: "CONSUMER",
        requestStatus: "ALL",
        operationType: undefined,
        isMyRequest: false,
      });
    });

    it("enables user to filter ACL requests by ACL type", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by ACL type",
      });
      expect(envFilter).toBeEnabled();

      await userEvent.selectOptions(envFilter, "CONSUMER");
      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "",
          env: "ALL",
          aclType: "CONSUMER",
          requestStatus: "ALL",
          operationType: undefined,
          isMyRequest: false,
        });
      });
    });
  });

  describe("user can filter ACL requests based on the status", () => {
    afterEach(() => {
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?status=APPROVED",
      });

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by status",
      });

      expect(envFilter).toHaveDisplayValue("Approved");

      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "APPROVED",
        operationType: undefined,
        isMyRequest: false,
      });
    });

    it("enables user to filter ACL requests by status", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by status",
      });
      expect(envFilter).toBeEnabled();

      await userEvent.selectOptions(envFilter, "APPROVED");

      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "",
          env: "ALL",
          aclType: "ALL",
          requestStatus: "APPROVED",
          operationType: undefined,
          isMyRequest: false,
        });
      });
    });
  });

  describe("user can filter ACL requests by only showing their own requests ", () => {
    afterEach(() => {
      cleanup();
    });

    it("renders proper state of toggle and filters correctly from the url search parameters", () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?showOnlyMyRequests=true",
      });

      const toggle = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });

      expect(toggle).toBeChecked();

      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        isMyRequest: true,
        operationType: undefined,
      });
    });

    it("enables user to filter ACL requests by only showing their own requests", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      const toggle = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });

      expect(toggle).not.toBeChecked();

      await userEvent.click(toggle);

      expect(toggle).toBeChecked();

      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "",
          env: "ALL",
          aclType: "ALL",
          requestStatus: "ALL",
          isMyRequest: true,
          operationType: undefined,
        });
      });
    });
  });

  describe("user can filter ACL requests by request type ", () => {
    afterEach(() => {
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?requestType=DELETE",
      });

      const envFilter = screen.getByRole("combobox", {
        name: "Filter by request type",
      });

      expect(envFilter).toHaveDisplayValue("Delete");

      expect(getAclRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "ALL",
        aclType: "ALL",
        requestStatus: "ALL",
        operationType: "DELETE",
        isMyRequest: false,
      });
    });

    it("enables user to filter ACL requests by request type", async () => {
      customRender(<AclRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      const requestTypeFilter = screen.getByRole("combobox", {
        name: "Filter by request type",
      });

      expect(requestTypeFilter).toBeEnabled();

      await userEvent.selectOptions(requestTypeFilter, "CREATE");

      await waitFor(() => {
        expect(getAclRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "",
          env: "ALL",
          aclType: "ALL",
          requestStatus: "ALL",
          operationType: "CREATE",
          isMyRequest: false,
        });
      });
    });
  });

  describe("shows a detail modal for ACL request", () => {
    beforeEach(async () => {
      mockGetAclRequests.mockResolvedValue(mockGetAclRequestsResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentsResponse);

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

    it("shows detail modal for first request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockGetAclRequestsResponse.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${firstRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(firstRequest.topicname);
    });

    it("shows detail modal for last request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const lastRequest =
        mockGetAclRequestsResponse.entries[
          mockGetAclRequestsResponse.entries.length - 1
        ];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${lastRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(lastRequest.topicname);
    });

    it("shows delete modal for last request returned from the api if clicking the Delete button", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const lastRequest =
        mockGetAclRequestsResponse.entries[
          mockGetAclRequestsResponse.entries.length - 1
        ];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${lastRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);

      const detailsModal = screen.getByRole("dialog");

      const detailsDeleteButton = within(detailsModal).getByRole("button", {
        name: "Delete",
      });

      await userEvent.click(detailsDeleteButton);

      const deleteModal = screen.getByRole("dialog");

      expect(deleteModal).toHaveTextContent(
        "Are you sure you want to delete the request?"
      );
    });
  });

  describe("shows a delete modal for ACL request", () => {
    beforeEach(async () => {
      mockGetAclRequests.mockResolvedValue(mockGetAclRequestsResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentsResponse);
      mockDeleteAclRequests.mockResolvedValue([{ success: true, message: "" }]);

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

    it("does not shows delete modal for first request returned from the api (deletable: false)", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockGetAclRequestsResponse.entries[0];
      const deleteRequestButton = screen.getByRole("button", {
        name: `Delete ACL request for ${firstRequest.topicname}`,
      });

      expect(deleteRequestButton).toBeDisabled();
    });

    it("shows delete modal for last request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const lastRequest =
        mockGetAclRequestsResponse.entries[
          mockGetAclRequestsResponse.entries.length - 1
        ];
      const deleteRequestButton = screen.getByRole("button", {
        name: `Delete ACL request for ${lastRequest.topicname}`,
      });

      await userEvent.click(deleteRequestButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(
        "Are you sure you want to delete the request?"
      );
    });

    it("deletes correct request modal for last request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const lastRequest =
        mockGetAclRequestsResponse.entries[
          mockGetAclRequestsResponse.entries.length - 1
        ];
      const deleteRequestButton = screen.getByRole("button", {
        name: `Delete ACL request for ${lastRequest.topicname}`,
      });

      await userEvent.click(deleteRequestButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(
        "Are you sure you want to delete the request?"
      );

      const deleteButton = within(modal).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(deleteButton);

      await waitFor(() => {
        expect(mockDeleteAclRequests).toHaveBeenLastCalledWith({
          reqIds: ["1217"],
        });
      });
    });
  });
});
