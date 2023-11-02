import { KlawApiResponse } from "types/utils";

const getMockedResponseGetClusterInfoFromEnvironment = (
  isAivenCluster: boolean
): KlawApiResponse<"getClusterInfoFromEnv"> => ({
  aivenCluster: isAivenCluster,
});

export { getMockedResponseGetClusterInfoFromEnvironment };
