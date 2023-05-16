import { Navigate, useMatches } from "react-router-dom";
import {
  isTopicsOverviewTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/overview/components/TopicOverviewResourceTabs";
import { TopicOverviewHeader } from "src/app/features/topics/overview/components/TopicOverviewHeader";

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

function TopicOverview(props: TopicOverviewProps) {
  const { topicName } = props;

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);
  if (currentTab === undefined) {
    return <Navigate to={`/topics`} replace={true} />;
  }
  return (
    <div>
      <TopicOverviewHeader topicName={topicName} />

      <TopicOverviewResourcesTabs
        currentTab={currentTab}
        topicName={topicName}
      />
    </div>
  );
}

export { TopicOverview };
