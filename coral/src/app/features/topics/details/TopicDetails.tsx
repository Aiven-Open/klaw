import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Navigate, useMatches, useOutletContext } from "react-router-dom";
import { TopicDetailsHeader } from "src/app/features/topics/details/components/TopicDetailsHeader";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import {
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverview } from "src/domain/topic";
import { getSchemaOfTopic, getTopicOverview } from "src/domain/topic/topic-api";
import { TopicSchemaOverview } from "src/domain/topic/topic-types";

type TopicOverviewProps = {
  topicName: string;
};

function findMatchingTab(
  matches: ReturnType<typeof useMatches>
): TopicOverviewTabEnum | undefined {
  const match = matches
    .map((match) => match.id)
    .find((id) =>
      Object.prototype.hasOwnProperty.call(TOPIC_OVERVIEW_TAB_ID_INTO_PATH, id)
    );
  if (isTopicsOverviewTabEnum(match)) {
    return match;
  }
  return undefined;
}

function TopicDetails(props: TopicOverviewProps) {
  const { topicName } = props;

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  const [environmentId, setEnvironmentId] = useState<string | undefined>(
    undefined
  );
  const [schemaVersion, setSchemaVersion] = useState<number | undefined>(
    undefined
  );

  const {
    data: topicData,
    isError: topicIsError,
    error: topicError,
    isLoading: topicIsLoading,
  } = useQuery(["topic-overview", environmentId], {
    queryFn: () => getTopicOverview({ topicName, environmentId }),
  });

  const {
    data: schemaData,
    isError: schemaIsError,
    error: schemaError,
    isLoading: schemaIsLoading,
  } = useQuery(
    [
      "schema-overview",
      topicData?.availableEnvironments,
      environmentId,
      schemaVersion,
    ],
    {
      queryFn: () => {
        const getKafkaEnvIds = () => {
          if (environmentId !== undefined) {
            return environmentId;
          }
          if (topicData?.availableEnvironments[0].id !== undefined) {
            return topicData?.availableEnvironments[0].id;
          }
          return "";
        };

        return getSchemaOfTopic({
          topicName,
          kafkaEnvId: getKafkaEnvIds(),
          schemaVersionSearch: schemaVersion,
        });
      },
      enabled: topicData?.availableEnvironments !== undefined,
    }
  );

  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }

  // If we rely on isLoading only,
  // when a user navigates to another topic overview after having previously seen a different topic
  // the TopicOverviewResourcesTabs rendered data will be stale for a second (showing previous topic data)
  // while the query is refetching the data
  // However, using isRefetching is also an issue, because then we will show the loading state every time the user switches environment
  // Instead of only the first time they switch
  // This isRefetchingTopicOverview variable ensures that we only show loading state when there is a desync between the topic name in pops
  // and the topic name in the available data
  const isRefetchingTopicOverview =
    topicData?.topicInfo.topicName !== topicName;

  return (
    <div>
      <TopicDetailsHeader
        topicName={topicName}
        topicExists={Boolean(topicData?.topicExists)}
        environments={topicData?.availableEnvironments}
        environmentId={environmentId}
        setEnvironmentId={setEnvironmentId}
      />

      <TopicOverviewResourcesTabs
        isLoading={
          topicIsLoading || schemaIsLoading || isRefetchingTopicOverview
        }
        isError={topicIsError || schemaIsError}
        error={topicError || schemaError}
        currentTab={currentTab}
        topicOverview={topicData}
        environmentId={environmentId}
        setSchemaVersion={setSchemaVersion}
        topicSchemas={schemaData}
      />
    </div>
  );
}

function useTopicDetails() {
  return useOutletContext<{
    environmentId: string;
    setSchemaVersion: (id: number) => void;
    topicOverview: TopicOverview;
    topicName: string;
    topicSchemas: TopicSchemaOverview;
  }>();
}

export { TopicDetails, useTopicDetails };
