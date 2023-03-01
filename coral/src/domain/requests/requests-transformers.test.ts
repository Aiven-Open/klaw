import { RequestsWaitingForApproval } from "src/domain/requests/requests-types";
import { KlawApiResponse } from "types/utils";
import { getRequestsWaitingForApprovalTransformer } from "src/domain/requests/requests-transformers";

const mockedRequestsWaitingForApproval: RequestsWaitingForApproval = {
  TOPIC: 0,
  ACL: 7,
  SCHEMA: 6,
  CONNECTOR: 2,
  USER: 0,
};

const mockedRequestsWaitingForApprovalMissingAcl: RequestsWaitingForApproval = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 6,
  CONNECTOR: 2,
  USER: 0,
};

const defaultRecord = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 0,
  CONNECTOR: 0,
  USER: 0,
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
});
