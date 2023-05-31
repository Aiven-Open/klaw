import { Navigate, useMatches, useOutletContext } from "react-router-dom";
import {
  isTopicsOverviewTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import { TopicDetailsHeader } from "src/app/features/topics/details/components/TopicDetailsHeader";
import { useQuery } from "@tanstack/react-query";
import { getTopicOverview } from "src/domain/topic/topic-api";
import { useState } from "react";
import { TopicOverview } from "src/domain/topic";

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

  const { data, isError, error, isLoading } = useQuery(
    ["topic-overview", environmentId],
    {
      queryFn: () => getTopicOverview({ topicName, environmentId }),
    }
  );

  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }

  return (
    <div>
      <TopicDetailsHeader
        topicName={topicName}
        topicExists={Boolean(data?.topicExists)}
        environments={data?.availableEnvironments}
        environmentId={environmentId}
        setEnvironmentId={setEnvironmentId}
      />

      <TopicOverviewResourcesTabs
        isLoading={isLoading}
        isError={isError}
        error={error}
        currentTab={currentTab}
        topicOverview={data}
        environmentId={environmentId}
      />
    </div>
  );
}

function useTopicDetails() {
  return useOutletContext<{
    environmentId: string;
    topicOverview: TopicOverview;
    topicName: string;
  }>();
}

export { TopicDetails, useTopicDetails };
