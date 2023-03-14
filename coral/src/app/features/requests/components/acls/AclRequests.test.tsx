import { cleanup } from "@testing-library/react";
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
    });
    expect(getAclRequests).toBeCalledTimes(1);
  });
});
