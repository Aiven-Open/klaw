import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { KlawApiModel } from "types/utils";
import { Environment } from "src/domain/environment/environment-types";

describe("environment-transformer.ts", () => {
  describe("transformEnvironmentApiResponse", () => {
    // even though the openapi definition defines `params` as required
    // some endpoints don't have a `params` property,
    // so we need to add a handling for that, too
    it("transforms API response objects into application domain model without params", () => {
      const emptyParamsEnv: KlawApiModel<"EnvModelResponse"> =
        createMockEnvironmentDTO({
          name: "DEV",
          id: "1337",
        });

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      delete emptyParamsEnv.params;

      const testInput: Omit<KlawApiModel<"EnvModelResponse">, "params">[] = [
        emptyParamsEnv,
      ];

      const expectedResult: Environment[] = [
        {
          name: "DEV",
          id: "1337",
          type: "kafka",
          clusterName: "DEV",
          tenantName: "default",
          envStatus: "ONLINE",
        },
      ];

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      expect(transformEnvironmentApiResponse(testInput)).toEqual(
        expectedResult
      );
    });

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
          clusterName: "DEV",
          tenantName: "default",
          envStatus: "ONLINE",
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
            topicRegex: ["\\bjon snow\\b"],
            applyRegex: false,
          },
          clusterName: "DEV",
          tenantName: "default",
          envStatus: "ONLINE",
        },
      ];

      expect(transformEnvironmentApiResponse(testInput)).toEqual(
        expectedResult
      );
    });

    it("transforms API response objects into application domain model and removes empty strings where needed", () => {
      const testInput: KlawApiModel<"EnvModelResponse">[] = [
        createMockEnvironmentDTO({
          name: "DEV",
          id: "1001",
          params: {
            defaultPartitions: "1",
            topicPrefix: ["dev-"],
            topicSuffix: ["-one", "", "", "-two"],
            topicRegex: [""],
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
            topicSuffix: ["-one", "-two"],
            topicRegex: [],
          },
          clusterName: "DEV",
          tenantName: "default",
          envStatus: "ONLINE",
        },
      ];
      expect(transformEnvironmentApiResponse(testInput)).toEqual(
        expectedResult
      );
    });
  });
});
