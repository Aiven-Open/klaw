import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { KlawApiModel } from "types/utils";
import { Environment } from "src/domain/environment/environment-types";

describe("environment-transformer.ts", () => {
  describe("transformEnvironmentApiResponse", () => {
    it("transforms API response objects into application domain model with a few params", () => {
      const testInput: KlawApiModel<"EnvModelResponse">[] = [
        createMockEnvironmentDTO({
          name: "DEV",
          id: "1001",
          params: {
            defaultPartitions: "1",
            topicPrefix: ["dev-"],
          },
        }),
      ];

      const expectedResult: Environment[] = [
        {
          name: "DEV",
          id: "1001",
          type: "kafka",
          params: {
            defaultPartitions: 1,
            topicPrefix: ["dev-"],
          },
        },
      ];
      expect(transformEnvironmentApiResponse(testInput)).toEqual(
        expectedResult
      );
    });

    it("transforms API response objects into application domain model with a all params", () => {
      const testInput: KlawApiModel<"EnvModelResponse">[] = [
        createMockEnvironmentDTO({
          name: "TST",
          id: "2002",
          params: {
            defaultPartitions: "1",
            maxPartitions: "2",
            partitionsList: ["hello"],
            defaultRepFactor: "3",
            maxRepFactor: "4",
            replicationFactorList: ["1", "2"],
            topicPrefix: ["pre_"],
            topicSuffix: ["_suffix"],
            topicRegex: ["\\bjon snow\\b"],
            applyRegex: false,
          },
        }),
      ];

      const expectedResult: Environment[] = [
        {
          name: "TST",
          id: "2002",
          type: "kafka",
          params: {
            defaultPartitions: 1,
            maxPartitions: 2,
            defaultRepFactor: 3,
            maxRepFactor: 4,
            topicPrefix: ["pre_"],
            topicSuffix: ["_suffix"],
          },
        },
      ];

      expect(transformEnvironmentApiResponse(testInput)).toEqual(
        expectedResult
      );
    });
  });
});
