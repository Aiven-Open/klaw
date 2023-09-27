import { Box, useToast } from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  Navigate,
  useLocation,
  useMatches,
  useOutletContext,
  useSearchParams,
} from "react-router-dom";
import { ClaimBanner } from "src/app/features/components/ClaimBanner";
import { ClaimConfirmationModal } from "src/app/features/components/ClaimConfirmationModal";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import {
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverview, TopicSchemaOverview } from "src/domain/topic";
import {
  getSchemaOfTopic,
  getTopicOverview,
  requestTopicClaim,
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
  // This state comes from the topic Link components in TopicTable
  const { state: initialEnvironment }: { state: string | null } = useLocation();
  const toast = useToast();

  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  const [schemaVersion, setSchemaVersion] = useState<number | undefined>(
    undefined
  );
  const [showClaimModal, setShowClaimModal] = useState(false);
  const [claimErrorMessage, setClaimErrorMessage] = useState<
    string | undefined
  >();

  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedEnvironmentId, setSelectedEnvironmentId] = useState(
    searchParams.get("env") ?? initialEnvironment ?? undefined
  );

  const {
    data: topicData,
    isError: topicIsError,
    error: topicError,
    isLoading: topicIsLoading,
    isRefetching: topicIsRefetching,
    refetch: refetchTopic,
  } = useQuery(["topic-overview", topicName, selectedEnvironmentId], {
    queryFn: () =>
      getTopicOverview({ topicName, environmentId: selectedEnvironmentId }),
  });

  const {
    data: schemaData,
    isError: schemaIsError,
    error: schemaError,
    isLoading: schemaIsLoading,
    isRefetching: schemaIsRefetching,
  } = useQuery(
    ["schema-overview", topicName, selectedEnvironmentId, schemaVersion],
    {
      queryFn: () => {
        if (selectedEnvironmentId !== undefined) {
          return getSchemaOfTopic({
            topicName,
            kafkaEnvId: selectedEnvironmentId,
            schemaVersionSearch: schemaVersion,
          });
        }
      },
      enabled: selectedEnvironmentId !== undefined,
    }
  );

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
      selectedEnvironmentId === undefined
    ) {
      setSelectedEnvironmentId(topicData.availableEnvironments[0].id);
    }
  }, [
    topicData?.availableEnvironments,
    setSelectedEnvironmentId,
    setSelectedEnvironmentId,
  ]);

  if (currentTab === undefined) {
    return <Navigate to={`/topic/${topicName}/overview`} replace={true} />;
  }

  return (
    <>
      {showClaimModal && (
        <ClaimConfirmationModal
          onSubmit={createClaimTopicRequest}
          onClose={() => setShowClaimModal(false)}
          isLoading={createClaimTopicRequestIsLoading}
          entity={"topic"}
        />
      )}
      <EntityDetailsHeader
        entity={{ name: topicName, type: "topic" }}
        entityExists={Boolean(topicData?.topicExists)}
        entityEditLink={`/topic/${topicName}/request-update?env=${selectedEnvironmentId}`}
        showEditButton={Boolean(topicData?.topicInfo.showEditTopic)}
        hasPendingRequest={Boolean(topicData?.topicInfo.hasOpenTopicRequest)}
        entityUpdating={topicIsRefetching}
        environments={topicData?.availableEnvironments}
        environmentId={selectedEnvironmentId}
        setEnvironmentId={(id: string) => {
          setSelectedEnvironmentId(id);
          setSearchParams({ env: id });
        }}
      />
      {topicData?.topicInfo !== undefined &&
        !topicData.topicInfo.topicOwner && (
          <Box marginBottom={"l1"}>
            <ClaimBanner
              entityType={"topic"}
              entityName={topicName}
              hasOpenClaimRequest={topicData?.topicInfo.hasOpenClaimRequest}
              entityOwner={topicData.topicInfo.teamname}
              hasOpenRequestOnAnyEnv={
                topicData?.topicInfo.hasOpenRequestOnAnyEnv
              }
              claimEntity={() => setShowClaimModal(true)}
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
        environmentId={selectedEnvironmentId}
        // These state setters are used refresh the queries with the correct params...
        // ...when a user selects schema version
        setSchemaVersion={setSchemaVersion}
        topicOverview={topicData}
        topicOverviewIsRefetching={topicIsRefetching}
        topicSchemas={schemaData}
        topicSchemasIsRefetching={schemaIsRefetching}
        topicName={topicName}
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
