import { Navigate, useMatches, useOutletContext } from "react-router-dom";
import {
  isTopicsOverviewTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import { TopicDetailsHeader } from "src/app/features/topics/details/components/TopicDetailsHeader";

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
  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }
  return (
    <div>
      <TopicDetailsHeader topicName={topicName} />

      <TopicOverviewResourcesTabs
        currentTab={currentTab}
        topicName={topicName}
      />
    </div>
  );
}

function useTopicDetails() {
  return useOutletContext<{ topicName: string }>();
}

export { TopicDetails, useTopicDetails };
