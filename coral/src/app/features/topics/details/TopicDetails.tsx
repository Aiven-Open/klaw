import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Navigate, useMatches, useOutletContext } from "react-router-dom";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { TopicDetailsHeader } from "src/app/features/topics/details/components/TopicDetailsHeader";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import {
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import {
  getAivenServiceAccountDetails,
  getConsumerOffsets,
} from "src/domain/acl/acl-api";
import {
  ConsumerOffsets,
  ServiceAccountDetails,
} from "src/domain/acl/acl-types";
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

  const user = useAuthContext();
  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  const [environmentId, setEnvironmentId] = useState<string | undefined>(
    undefined
  );
  const [schemaVersion, setSchemaVersion] = useState<number | undefined>(
    undefined
  );
  const [consumerGroupId, setConsumerGroupId] = useState<string | undefined>(
    undefined
  );

  const [aclReqNo, setAclReqNo] = useState<string | undefined>(undefined);

  const {
    data: topicData,
    isError: topicIsError,
    error: topicError,
    isLoading: topicIsLoading,
    isFetched: topicDataFetched,
  } = useQuery(["topic-overview", environmentId], {
    queryFn: () => getTopicOverview({ topicName, environmentId }),
  });

  const {
    data: offsetsData,
    isError: offsetsIsError,
    error: offsetsError,
    isFetched: offsetDataFetched,
  } = useQuery(["consumer-offsets", environmentId, consumerGroupId], {
    queryFn: () => {
      if (environmentId !== undefined && consumerGroupId !== undefined) {
        return getConsumerOffsets({
          topicName,
          env: environmentId,
          consumerGroupId,
        });
      }
    },
    enabled: environmentId !== undefined && consumerGroupId !== undefined,
  });

  const {
    data: serviceAccountData,
    isError: serviceAccountIsError,
    error: serviceAccountError,
    isFetched: serviceAccountDataFetched,
  } = useQuery(
    [
      "getAivenServiceAccountDetails",
      environmentId,
      topicName,
      user?.username,
      aclReqNo,
    ],
    {
      queryFn: () => {
        if (
          environmentId !== undefined &&
          aclReqNo !== undefined &&
          user?.username !== undefined
        ) {
          return getAivenServiceAccountDetails({
            env: environmentId,
            topicName,
            userName: user.username,
            aclReqNo,
          });
        }
      },
      enabled: environmentId !== undefined && aclReqNo !== undefined,
    }
  );

  const {
    data: schemaData,
    isError: schemaIsError,
    error: schemaError,
    isLoading: schemaIsLoading,
    isFetched: schemaDataFetched,
  } = useQuery(["schema-overview", environmentId, schemaVersion], {
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

  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }

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
        isLoading={topicIsLoading || schemaIsLoading}
        isError={
          topicIsError ||
          schemaIsError ||
          offsetsIsError ||
          serviceAccountIsError
        }
        error={topicError || schemaError || offsetsError || serviceAccountError}
        currentTab={currentTab}
        environmentId={environmentId}
        // These state setters are used refresh the queries with the correct params...
        // ...when a user selects schema version / clicks on Details action in Subscription tab
        setSchemaVersion={setSchemaVersion}
        setConsumerGroupId={setConsumerGroupId}
        setAclReqNo={setAclReqNo}
        // We pass undefined when data is not fetched to avoid flash of stale data in the UI
        topicOverview={topicDataFetched ? topicData : undefined}
        topicSchemas={schemaDataFetched ? schemaData : undefined}
        offsetsData={offsetDataFetched ? (offsetsData || [])[0] : undefined}
        serviceAccountData={
          serviceAccountDataFetched ? serviceAccountData : undefined
        }
      />
    </div>
  );
}

function useTopicDetails() {
  return useOutletContext<{
    environmentId: string;
    setSchemaVersion: (id: number) => void;
    setConsumerGroupId: (id?: string) => void;
    setAclReqNo: (aclReqNo?: string) => void;
    topicOverview: TopicOverview;
    topicName: string;
    topicSchemas: TopicSchemaOverview;
    offsetsData?: ConsumerOffsets;
    serviceAccountData?: ServiceAccountDetails;
  }>();
}

export { TopicDetails, useTopicDetails };
