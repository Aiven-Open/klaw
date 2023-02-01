import { useQueries, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Environment, getEnvironments } from "src/domain/environment";
import { getTopicNames, TopicNames } from "src/domain/topic";

interface ScopedTopicNames {
  environmentId: string;
  topicNames: string[];
}

const useEnvironmentTopics = () => {
  const [scopedTopicNames, setScopedTopicNames] = useState<ScopedTopicNames[]>(
    []
  );
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

  const topicNamesData = useQueries({ queries: topicNamesQueries });
  const scopedTopicNamesIsLoading = topicNamesData.some(
    (data) => data.isLoading
  );
  const validEnvironments = (environments || []).filter((env) =>
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
  };
};

export default useEnvironmentTopics;
