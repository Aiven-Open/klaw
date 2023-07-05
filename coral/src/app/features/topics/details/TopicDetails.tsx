import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  Navigate,
  useLocation,
  useMatches,
  useOutletContext,
} from "react-router-dom";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";
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
  const { state: initialEnvironment }: { state: string | null } = useLocation();

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  const [environmentId, setEnvironmentId] = useState<string | undefined>(
    initialEnvironment ?? undefined
  );
  const [schemaVersion, setSchemaVersion] = useState<number | undefined>(
    undefined
  );

  const {
    data: topicData,
    isError: topicIsError,
    error: topicError,
    isLoading: topicIsLoading,
  } = useQuery(["topic-overview", topicName, environmentId], {
    queryFn: () => getTopicOverview({ topicName, environmentId }),
  });

  const {
    data: schemaData,
    isError: schemaIsError,
    error: schemaError,
    isLoading: schemaIsLoading,
  } = useQuery(["schema-overview", topicName, environmentId, schemaVersion], {
    queryFn: () => {
      if (environmentId !== undefined) {
        return getSchemaOfTopic({
          topicName,
          kafkaEnvId: environmentId,
          schemaVersionSearch: schemaVersion,
        });
      }
    },
    enabled: environmentId !== undefined,
  });

  useEffect(() => {
    if (
      topicData?.availableEnvironments !== undefined &&
      environmentId === undefined
    ) {
      setEnvironmentId(topicData.availableEnvironments[0].id);
    }
  }, [topicData?.availableEnvironments, environmentId, setEnvironmentId]);

  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }

  return (
    <div>
      <EntityDetailsHeader
        entity={{ name: topicName, type: "topic" }}
        entityExists={Boolean(topicData?.topicExists)}
        entityEditLink={
          "/topicOverview/topicname=SchemaTest&env=${topicOverview.availableEnvironments[0].id}&requestType=edit"
        }
        environments={topicData?.availableEnvironments}
        environmentId={environmentId}
        setEnvironmentId={setEnvironmentId}
      />

      <TopicOverviewResourcesTabs
        isLoading={topicIsLoading || schemaIsLoading}
        isError={topicIsError || schemaIsError}
        error={topicError || schemaError}
        currentTab={currentTab}
        environmentId={environmentId}
        // These state setters are used refresh the queries with the correct params...
        // ...when a user selects schema version
        setSchemaVersion={setSchemaVersion}
        topicOverview={topicData}
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
