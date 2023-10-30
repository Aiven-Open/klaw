import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { transformPaginatedEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";

describe("environment-transformer.ts", () => {
  describe("transformPaginatedEnvironmentApiResponse", () => {
    const apiResponse: Environment[] = [
      createMockEnvironmentDTO({
        name: "DEV",
        totalNoPages: "1",
        currentPage: "1",
        totalRecs: 2,
        allPageNos: ["1"],
      }),
      createMockEnvironmentDTO({
        name: "TST",
        totalNoPages: "1",
        currentPage: "1",
        totalRecs: 2,
        allPageNos: ["1"],
      }),
    ];

    it("should format environments with pagination", () => {
      const result = transformPaginatedEnvironmentApiResponse(apiResponse);

      expect(result).toEqual({
        totalPages: Number(apiResponse[0].totalNoPages),
        currentPage: Number(apiResponse[0].currentPage),
        totalEnvs: Number(apiResponse[0].totalRecs),
        entries: apiResponse,
      });
    });
  });
});
