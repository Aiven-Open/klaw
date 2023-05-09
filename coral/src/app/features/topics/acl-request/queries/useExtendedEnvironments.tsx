import { useQuery } from "@tanstack/react-query";
import {
  Environment,
  getAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import { getClusterInfo } from "src/domain/environment/environment-api";
import { getTopicNames } from "src/domain/topic";

interface ExtendedEnvironment extends Environment {
  isAivenCluster: boolean;
  topicNames: string[];
}

interface GetExtensionDataParams {
  envId: Environment["id"];
  envType: Environment["type"];
}

const getExtensionData = ({ envId, envType }: GetExtensionDataParams) => {
  return Promise.all([
    getTopicNames({
      envSelected: envId,
      onlyMyTeamTopics: false,
    }),
    getClusterInfo({
      envType,
      envSelected: envId as string,
    }),
  ]);
};

const useExtendedEnvironments = () => {
  const {
    data: extendedEnvironments = [],
    isLoading: isLoadingExtendedEnvironments,
  } = useQuery<ExtendedEnvironment[], Error>(["topic-environments"], {
    queryFn: async () => {
      const environments = await getAllEnvironmentsForTopicAndAcl();

      const extensionRequests = environments.map(async (environment) => {
        const [topicNames, clusterInfo] = await getExtensionData({
          envId: environment.id,
          envType: environment.type,
        });
        return {
          ...environment,
          topicNames,
          isAivenCluster: clusterInfo.aivenCluster,
        };
      });

      return Promise.all(extensionRequests);
    },
    // Only need to be fetched once on page load
    // So we set staleTime to Infinity
    staleTime: Infinity,
  });

  return {
    extendedEnvironments,
    isLoadingExtendedEnvironments,
  };
};

export default useExtendedEnvironments;
