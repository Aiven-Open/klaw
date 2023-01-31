import { Box } from "@aivenio/aquarium";
import { useQueries, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import SkeletonForm from "src/app/features/topics/acl-request/forms/SkeletonForm";
import TopicConsumerForm from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import TopicProducerForm from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
import {
  ClusterInfo,
  Environment,
  getEnvironments,
} from "src/domain/environment";
import { getClusterInfo } from "src/domain/environment/environment-api";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";
import {
  getTopicNames,
  getTopicTeam,
  TopicNames,
  TopicTeam,
} from "src/domain/topic";

interface ScopedTopicNames {
  environmentId: string;
  topicNames: string[];
}

const TopicAclRequest = () => {
  const { topicName = "" } = useParams();
  const [topicType, setTopicType] = useState("Producer");
  const [scopedTopicNames, setScopedTopicNames] = useState<ScopedTopicNames[]>(
    []
  );

  const topicProducerForm = useForm<TopicProducerFormSchema>({
    schema: topicProducerFormSchema,
    defaultValues: {
      topicname: topicName,
      environment: ENVIRONMENT_NOT_INITIALIZED,
      topictype: "Producer",
    },
  });

  const topicConsumerForm = useForm<TopicConsumerFormSchema>({
    schema: topicConsumerFormSchema,
    defaultValues: {
      aclPatternType: "LITERAL",
      topicname: topicName,
      environment: ENVIRONMENT_NOT_INITIALIZED,
      topictype: "Consumer",
      consumergroup: "-na-",
    },
  });

  const { data: environments, isLoading: environmentsIsLoading } = useQuery<
    Environment[],
    Error
  >(["topic-environments"], {
    queryFn: getEnvironments,
  });

  const topicNamesQueries =
    environments === undefined
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
  const topicNamesIsLoading = topicNamesData.some((data) => data.isLoading);

  const selectedPatternType =
    topicType === "Producer"
      ? topicProducerForm.watch("aclPatternType")
      : "LITERAL";
  const { isLoading: topicTeamIsLoading } = useQuery<TopicTeam, Error>({
    queryKey: ["topicTeam", topicName, selectedPatternType, topicType],
    queryFn: () =>
      getTopicTeam({ topicName, patternType: selectedPatternType }),
    onSuccess: (data) => {
      if (data === undefined) {
        throw new Error("Could not fetch team for current Topic");
      }
      return topicType === "Producer"
        ? topicProducerForm.setValue("teamname", data.team)
        : topicConsumerForm.setValue("teamname", data.team);
    },
    keepPreviousData: true,
  });

  const selectedEnvironment =
    topicType === "Producer"
      ? topicProducerForm.watch("environment")
      : topicConsumerForm.watch("environment");
  // We cast the type of selectedEnvironmentType to be Environment["type"]
  // Because there should be no case where this returns undefined
  // As an additional safety, this query is disabled when it *is* undefined
  const selectedEnvironmentType = environments?.find(
    (env) => env.id === selectedEnvironment
  )?.type as Environment["type"];
  const { data: clusterInfo } = useQuery<ClusterInfo, Error>(
    ["cluster-info", selectedEnvironment, topicType],
    {
      queryFn: () =>
        getClusterInfo({
          envSelected: selectedEnvironment,
          envType: selectedEnvironmentType,
        }),
      keepPreviousData: true,
      enabled:
        selectedEnvironment !== ENVIRONMENT_NOT_INITIALIZED &&
        environments !== undefined &&
        selectedEnvironmentType !== undefined,
      onSuccess: (data) => {
        const isAivenCluster = data?.aivenCluster === "true";
        // Enable the only possible options when the environment chosen is Aiven Kafka flavor
        if (isAivenCluster) {
          if (topicType === "Producer") {
            topicProducerForm.setValue("aclPatternType", "LITERAL");
            topicProducerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
            topicProducerForm.resetField("transactionalId");
          }
          if (topicType === "Consumer") {
            topicConsumerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
          }
        }
      },
    }
  );

  if (environmentsIsLoading || topicTeamIsLoading || topicNamesIsLoading) {
    return <SkeletonForm />;
  }

  const currentTopicNames =
    scopedTopicNames.find(
      (scoped) => scoped.environmentId === selectedEnvironment
    )?.topicNames || [];

  const validEnvironments = (environments || []).filter((env) =>
    new Set(
      scopedTopicNames.map((scoped) =>
        scoped.topicNames.length > 0 ? scoped.environmentId : undefined
      )
    ).has(env.id)
  );

  return (
    <Box maxWidth={"4xl"}>
      {topicType === "Consumer" ? (
        <TopicConsumerForm
          renderAclTypeField={() => (
            <AclTypeField topicType={topicType} handleChange={setTopicType} />
          )}
          topicConsumerForm={topicConsumerForm}
          topicNames={currentTopicNames}
          environments={validEnvironments}
          clusterInfo={clusterInfo}
        />
      ) : (
        <TopicProducerForm
          renderAclTypeField={() => (
            <AclTypeField topicType={topicType} handleChange={setTopicType} />
          )}
          topicProducerForm={topicProducerForm}
          topicNames={currentTopicNames}
          environments={validEnvironments}
          clusterInfo={clusterInfo}
        />
      )}
    </Box>
  );
};

export default TopicAclRequest;
