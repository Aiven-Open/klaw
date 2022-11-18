import { EnvironmentDTO } from "src/domain/environment/environment-types";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";

describe("environment-transformer.ts", () => {
  describe("'transformEnvironmentApiResponse' transforms API response into list of environments", () => {
    it("transforms list of 4 environments with 2 unique environment values", () => {
      const testInput: EnvironmentDTO[] = [
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("TST"),
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("DEV"),
      ];

      expect(transformEnvironmentApiResponse(testInput)).toEqual([
        "DEV",
        "TST",
      ]);
    });

    it("transforms list of 3 environments with 1 unique environment value", () => {
      const testInput: EnvironmentDTO[] = [
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("DEV"),
        createMockEnvironmentDTO("DEV"),
      ];

      expect(transformEnvironmentApiResponse(testInput)).toEqual(["DEV"]);
    });
  });
});
