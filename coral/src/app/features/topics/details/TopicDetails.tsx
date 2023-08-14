import { Box, useToast } from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  Navigate,
  useLocation,
  useMatches,
  useOutletContext,
} from "react-router-dom";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";
import TopicClaimBanner from "src/app/features/topics/details/components/TopicClaimBanner";
import { TopicClaimConfirmationModal } from "src/app/features/topics/details/components/TopicClaimConfirmationModal";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import {
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverview, TopicSchemaOverview } from "src/domain/topic";
import {
  requestTopicClaim,
  getSchemaOfTopic,
  getTopicOverview,
} from "src/domain/topic/topic-api";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

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
  const toast = useToast();

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  const [environmentId, setEnvironmentId] = useState<string | undefined>(
    initialEnvironment ?? undefined
  );
  const [schemaVersion, setSchemaVersion] = useState<number | undefined>(
    undefined
  );
  const [showClaimModal, setShowClaimModal] = useState(false);
  const [claimErrorMessage, setClaimErrorMessage] = useState<
    string | undefined
  >();

  const {
    data: topicData,
    isError: topicIsError,
    error: topicError,
    isLoading: topicIsLoading,
    isRefetching: topicIsRefetching,
    refetch: refetchTopic,
  } = useQuery(["topic-overview", topicName, environmentId], {
    queryFn: () => getTopicOverview({ topicName, environmentId }),
  });

  const {
    data: schemaData,
    isError: schemaIsError,
    error: schemaError,
    isLoading: schemaIsLoading,
    isRefetching: schemaIsRefetching,
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

  const {
    mutate: createClaimTopicRequest,
    isLoading: createClaimTopicRequestIsLoading,
    isError: createClaimTopicRequestIsError,
  } = useMutation(
    (remark?: string) =>
      requestTopicClaim({
        topicName: topicData?.topicInfo.topicName || "",
        env: topicData?.topicInfo.envId || "",
        remark,
      }),
    {
      onSuccess: () => {
        refetchTopic().then(() => {
          setShowClaimModal(false);
          toast({
            message: "Topic claim request successfully created",
            position: "bottom-left",
            variant: "default",
          });
        });
      },
      onError: (error: HTTPError) => {
        setShowClaimModal(false);
        setClaimErrorMessage(parseErrorMsg(error));
      },
    }
  );

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
    <>
      {showClaimModal && (
        <TopicClaimConfirmationModal
          onSubmit={createClaimTopicRequest}
          onClose={() => setShowClaimModal(false)}
          isLoading={createClaimTopicRequestIsLoading}
        />
      )}
      <EntityDetailsHeader
        entity={{ name: topicName, type: "topic" }}
        entityExists={Boolean(topicData?.topicExists)}
        entityEditLink={`/topic/${topicName}/request-update?env=${topicData?.topicInfo.envId}`}
        showEditButton={Boolean(
          topicData?.topicInfo.showEditTopic &&
            !topicData?.topicInfo.hasOpenTopicRequest
        )}
        entityUpdating={topicIsRefetching}
        environments={topicData?.availableEnvironments}
        environmentId={environmentId}
        setEnvironmentId={setEnvironmentId}
      />
      {topicData?.topicInfo !== undefined &&
        !topicData.topicInfo.topicOwner && (
          <Box marginBottom={"l1"}>
            <TopicClaimBanner
              topicName={topicName}
              hasOpenClaimRequest={topicData?.topicInfo.hasOpenClaimRequest}
              topicOwner={topicData.topicInfo.teamname}
              hasOpenRequest={topicData?.topicInfo.hasOpenRequest}
              setShowClaimModal={setShowClaimModal}
              isError={createClaimTopicRequestIsError}
              errorMessage={claimErrorMessage}
            />
          </Box>
        )}
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
        topicOverviewIsRefetching={topicIsRefetching}
        topicSchemas={schemaData}
        topicSchemasIsRefetching={schemaIsRefetching}
      />
    </>
  );
}

function useTopicDetails() {
  return useOutletContext<{
    environmentId: string;
    setSchemaVersion: (id: number) => void;
    topicOverview: TopicOverview;
    topicOverviewIsRefetching: boolean;
    topicName: string;
    topicSchemas: TopicSchemaOverview;
    topicSchemasIsRefetching: boolean;
  }>();
}

export { TopicDetails, useTopicDetails };
