import { RequestsWaitingForApprovalWithTotal } from "src/domain/requests";
import { KlawApiResponse } from "types/utils";
import {
  activityLogTransformer,
  getRequestsWaitingForApprovalTransformer,
} from "src/domain/requests/requests-transformers";

const mockedRequestsWaitingForApproval: RequestsWaitingForApprovalWithTotal = {
  TOPIC: 0,
  ACL: 7,
  SCHEMA: 6,
  CONNECTOR: 2,
  USER: 0,
  OPERATIONAL: 0,
  TOTAL_NOTIFICATIONS: 15,
};

const mockedRequestsWaitingForApprovalMissingAcl: RequestsWaitingForApprovalWithTotal =
  {
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 6,
    CONNECTOR: 2,
    USER: 0,
    OPERATIONAL: 0,
    TOTAL_NOTIFICATIONS: 8,
  };

const defaultRecord = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 0,
  CONNECTOR: 0,
  USER: 0,
  OPERATIONAL: 0,
  TOTAL_NOTIFICATIONS: 0,
};

const mockApiResponse: KlawApiResponse<"getRequestStatistics"> = {
  requestEntityStatistics: [
    {
      requestEntityType: "ACL",
      requestStatusCountSet: [
        {
          requestStatus: "CREATED",
          count: 7,
        },
        {
          requestStatus: "DECLINED",
          count: 81,
        },
        {
          requestStatus: "APPROVED",
          count: 47,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
        {
          requestStatus: "DELETED",
          count: 50,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "DELETE",
          count: 0,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 0,
        },
        {
          requestOperationType: "CREATE",
          count: 189,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
      ],
    },
    {
      requestEntityType: "SCHEMA",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 10,
        },
        {
          requestStatus: "DELETED",
          count: 13,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
        {
          requestStatus: "CREATED",
          count: 6,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "DELETE",
          count: 0,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 0,
        },
        {
          requestOperationType: "CREATE",
          count: 29,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
      ],
    },
    {
      requestEntityType: "TOPIC",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 10,
        },
        {
          requestStatus: "DELETED",
          count: 22,
        },
        {
          requestStatus: "CREATED",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 24,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "CREATE",
          count: 50,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "DELETE",
          count: 1,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 1,
        },
      ],
    },
    {
      requestEntityType: "CONNECTOR",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 0,
        },
        {
          requestStatus: "DELETED",
          count: 0,
        },
        {
          requestStatus: "CREATED",
          count: 2,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 1,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "DELETE",
          count: 0,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 0,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
        {
          requestOperationType: "CREATE",
          count: 3,
        },
      ],
    },
  ],
};

const mockApiResponseMissingAcl: KlawApiResponse<"getRequestStatistics"> = {
  requestEntityStatistics: [
    {
      requestEntityType: "SCHEMA",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 10,
        },
        {
          requestStatus: "DELETED",
          count: 13,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
        {
          requestStatus: "CREATED",
          count: 6,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "DELETE",
          count: 0,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 0,
        },
        {
          requestOperationType: "CREATE",
          count: 29,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
      ],
    },
    {
      requestEntityType: "TOPIC",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 10,
        },
        {
          requestStatus: "DELETED",
          count: 22,
        },
        {
          requestStatus: "CREATED",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 24,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "CREATE",
          count: 50,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "DELETE",
          count: 1,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 1,
        },
      ],
    },
    {
      requestEntityType: "CONNECTOR",
      requestStatusCountSet: [
        {
          requestStatus: "DECLINED",
          count: 0,
        },
        {
          requestStatus: "DELETED",
          count: 0,
        },
        {
          requestStatus: "CREATED",
          count: 2,
        },
        {
          requestStatus: "ALL",
          count: 0,
        },
        {
          requestStatus: "APPROVED",
          count: 1,
        },
      ],
      requestsOperationTypeCountSet: [
        {
          requestOperationType: "DELETE",
          count: 0,
        },
        {
          requestOperationType: "UPDATE",
          count: 0,
        },
        {
          requestOperationType: "CLAIM",
          count: 0,
        },
        {
          requestOperationType: "PROMOTE",
          count: 0,
        },
        {
          requestOperationType: "CREATE",
          count: 3,
        },
      ],
    },
  ],
};
const emptyActivityLog = {
  totalPages: 0,
  currentPage: 0,
  entries: [],
};

const mockActivityLogResponse = [
  {
    req_no: 1001,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T13:52:14.646+00:00",
    activityTimeString: "17-Jun-2023 13:52:14",
    details: "testtopic12345",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "DEV",
    team: "Ospo",
    totalNoPages: "21",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
  {
    req_no: 1002,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T14:12:11.625+00:00",
    activityTimeString: "17-Jun-2023 14:12:11",
    details: "test4333",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "DEV",
    team: "Ospo",
    totalNoPages: "21",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
  {
    req_no: 1003,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T22:32:34.887+00:00",
    activityTimeString: "17-Jun-2023 22:32:34",
    details: "testaivtopic4366",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "DEV",
    team: "Ospo",
    totalNoPages: "21",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
  {
    req_no: 1004,
    tenantId: 101,
    activityName: "AclRequest",
    activityType: "Create",
    activityTime: "2023-06-20T08:46:31.670+00:00",
    activityTimeString: "20-Jun-2023 08:46:31",
    details: "127.0.0.1-SchemaTest-User:*-kwconsumergroup-Consumer",
    user: "muralibasani",
    teamId: 1005,
    env: "2",
    envName: "TST",
    team: "Ospo",
    totalNoPages: "21",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
];

const mockTransformedActivityLogResponse = {
  totalPages: 21,
  currentPage: 1,
  entries: [
    {
      req_no: 1001,
      tenantId: 101,
      activityName: "TopicRequest",
      activityType: "Create",
      activityTime: "2023-06-17T13:52:14.646+00:00",
      activityTimeString: "17-Jun-2023 13:52:14",
      details: "testtopic12345",
      user: "muralibasani",
      teamId: 1005,
      env: "1",
      envName: "DEV",
      team: "Ospo",
      totalNoPages: "21",
      currentPage: "1",
      allPageNos: ["1", ">", ">>"],
    },
    {
      req_no: 1002,
      tenantId: 101,
      activityName: "TopicRequest",
      activityType: "Create",
      activityTime: "2023-06-17T14:12:11.625+00:00",
      activityTimeString: "17-Jun-2023 14:12:11",
      details: "test4333",
      user: "muralibasani",
      teamId: 1005,
      env: "1",
      envName: "DEV",
      team: "Ospo",
      totalNoPages: "21",
      currentPage: "1",
      allPageNos: ["1", ">", ">>"],
    },
    {
      req_no: 1003,
      tenantId: 101,
      activityName: "TopicRequest",
      activityType: "Create",
      activityTime: "2023-06-17T22:32:34.887+00:00",
      activityTimeString: "17-Jun-2023 22:32:34",
      details: "testaivtopic4366",
      user: "muralibasani",
      teamId: 1005,
      env: "1",
      envName: "DEV",
      team: "Ospo",
      totalNoPages: "21",
      currentPage: "1",
      allPageNos: ["1", ">", ">>"],
    },
    {
      req_no: 1004,
      tenantId: 101,
      activityName: "AclRequest",
      activityType: "Create",
      activityTime: "2023-06-20T08:46:31.670+00:00",
      activityTimeString: "20-Jun-2023 08:46:31",
      details: "127.0.0.1-SchemaTest-User:*-kwconsumergroup-Consumer",
      user: "muralibasani",
      teamId: 1005,
      env: "2",
      envName: "TST",
      team: "Ospo",
      totalNoPages: "21",
      currentPage: "1",
      allPageNos: ["1", ">", ">>"],
    },
  ],
};

describe("request-transformers.ts", () => {
  describe("'getRequestsWaitingForApprovalTransformer' transforms API response into record of {TYPE: amount of requests}", () => {
    it("should return the correct record", () => {
      const response =
        getRequestsWaitingForApprovalTransformer(mockApiResponse);

      expect(response).toEqual(mockedRequestsWaitingForApproval);
    });

    it("should return record with default data if API response is undefined", () => {
      const response = getRequestsWaitingForApprovalTransformer({
        requestEntityStatistics: undefined,
      });

      expect(response).toEqual(defaultRecord);
    });

    it("should return record with default data for single entity type if API response misses that data", () => {
      const response = getRequestsWaitingForApprovalTransformer(
        mockApiResponseMissingAcl
      );

      expect(response).toEqual(mockedRequestsWaitingForApprovalMissingAcl);
    });
  });

  describe("'activityLogTransformer' transforms API response into paginated response", () => {
    it("should return paginated reponse", () => {
      const response = activityLogTransformer(mockActivityLogResponse);

      expect(response).toEqual(mockTransformedActivityLogResponse);
    });

    it("should return record with empty data if API response is empty array", () => {
      const response = activityLogTransformer([]);

      expect(response).toEqual(emptyActivityLog);
    });
  });
});
