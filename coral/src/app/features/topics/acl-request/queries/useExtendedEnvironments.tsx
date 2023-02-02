import { useQueries, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import {
  ClusterInfo,
  Environment,
  getEnvironments,
} from "src/domain/environment";
import { getClusterInfo } from "src/domain/environment/environment-api";
import { getTopicNames, TopicNames } from "src/domain/topic";

interface ScopedTopicNames {
  environmentId: string;
  topicNames: string[];
}

interface EnvironmentWithClusterInfo extends Environment {
  isAivenCluster: boolean;
}

interface ExtendedEnvironment extends Environment {
  isAivenCluster: boolean;
  topicNames: string[];
}

const useExtendedEnvironments = () => {
  const [scopedTopicNames, setScopedTopicNames] = useState<ScopedTopicNames[]>(
    []
  );
  const [environmentsWithClusterInfo, setEnvironmentsWithClusterInfo] =
    useState<EnvironmentWithClusterInfo[]>([]);

  const { data: environments, isLoading: isLoadingEnvironments } = useQuery<
    Environment[],
    Error
  >(["topic-environments"], {
    queryFn: getEnvironments,
    // Only need to be fetched once on page load
    // So we set staleTime to Infinity
    staleTime: Infinity,
  });

  const topicNamesQueries =
    environments === undefined || isLoadingEnvironments
      ? []
      : environments.map((env) => {
          return {
            queryKey: ["topicNames", env.id],
            queryFn: () =>
              getTopicNames({
                onlyMyTeamTopics: false,
                envSelected: env.id,
              }),
            onSuccess: (data: TopicNames) => {
              setScopedTopicNames((prev) => [
                ...prev,
                { environmentId: env.id, topicNames: data },
              ]);
            },
            // Only need to be fetched once on page load
            // So we set staleTime to Infinity
            staleTime: Infinity,
          };
        });

  const clusterInfoQueries =
    environments === undefined || isLoadingEnvironments
      ? []
      : environments.map((env) => {
          return {
            queryKey: ["clusterInfo", env.id],
            queryFn: () =>
              getClusterInfo({
                envSelected: env.id,
                envType: env.type,
              }),
            onSuccess: (data: ClusterInfo) => {
              setEnvironmentsWithClusterInfo((prev) => [
                ...prev,
                { ...env, isAivenCluster: data.aivenCluster === "true" },
              ]);
            },
            // Only need to be fetched once on page load
            // So we set staleTime to Infinity
            staleTime: Infinity,
          };
        });

  const topicNamesData = useQueries({ queries: topicNamesQueries });
  const isLoadingScopedTopicNames = topicNamesData.some(
    (data) => data.isLoading
  );
  const clusterInfoData = useQueries({ queries: clusterInfoQueries });
  const isLoadingClusterInfo = clusterInfoData.some((data) => data.isLoading);

  const isLoadingExtendedEnvironments =
    isLoadingScopedTopicNames || isLoadingEnvironments || isLoadingClusterInfo;

  let extendedEnvironments: ExtendedEnvironment[] = [];

  if (!isLoadingExtendedEnvironments) {
    const computedExtendedEnvironments: ExtendedEnvironment[] =
      environmentsWithClusterInfo.reduce(
        (
          extendedEnvironments: ExtendedEnvironment[],
          currentEnvironment: EnvironmentWithClusterInfo
        ) => {
          const topicNames = scopedTopicNames.find(
            ({ environmentId }) => environmentId === currentEnvironment.id
          )?.topicNames;

          if (topicNames === undefined || topicNames.length === 0) {
            return extendedEnvironments;
          }

          return [
            ...extendedEnvironments,
            { ...currentEnvironment, topicNames },
          ];
        },
        []
      );

    extendedEnvironments = computedExtendedEnvironments;
  }

  return {
    extendedEnvironments,
    isLoadingExtendedEnvironments,
  };
};

export default useExtendedEnvironments;
