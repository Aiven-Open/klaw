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

const useEnvironmentTopics = () => {
  const [scopedTopicNames, setScopedTopicNames] = useState<ScopedTopicNames[]>(
    []
  );
  const [environmentsWithClusterInfo, setEnvironmentsWithClusterInfo] =
    useState<EnvironmentWithClusterInfo[]>([]);

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >(["topic-environments"], {
    queryFn: getEnvironments,
  });

  const topicNamesQueries =
    environments === undefined || environmentsIsLoading
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
          };
        });

  const clusterInfoQueries =
    environments === undefined || environmentsIsLoading
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
          };
        });

  const topicNamesData = useQueries({ queries: topicNamesQueries });
  const scopedTopicNamesIsLoading = topicNamesData.some(
    (data) => data.isLoading
  );
  const clusterInfoData = useQueries({ queries: clusterInfoQueries });
  const clusterInfoIsLoading = clusterInfoData.some((data) => data.isLoading);

  const validEnvironments = environmentsWithClusterInfo.filter((env) =>
    new Set(
      scopedTopicNames.map((scoped) =>
        scoped.topicNames.length > 0 ? scoped.environmentId : undefined
      )
    ).has(env.id)
  );

  return {
    scopedTopicNames,
    scopedTopicNamesIsLoading,
    environmentsIsLoading,
    validEnvironments,
    clusterInfoIsLoading,
  };
};

export default useEnvironmentTopics;
