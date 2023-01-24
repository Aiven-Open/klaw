import { Box } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
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
  mockGetEnvironments,
} from "src/domain/environment";
import { getClusterInfo } from "src/domain/environment/environment-api";
import {
  getMockedResponseGetClusterInfoFromEnv,
  mockGetClusterInfoFromEnv,
} from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";
import {
  getTopicNames,
  getTopicTeam,
  TopicNames,
  TopicTeam,
} from "src/domain/topic";
import {
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
  mockGetTopicNames,
  mockGetTopicTeam,
} from "src/domain/topic/topic-api.msw";

const mockedData = [
  createMockEnvironmentDTO({
    name: "TST",
    id: "1",
    maxPartitions: "6",
    maxReplicationFactor: "2",
    defaultPartitions: "3",
    defaultReplicationFactor: "2",
  }),
  createMockEnvironmentDTO({
    name: "DEV",
    id: "2",
    maxPartitions: undefined,
    maxReplicationFactor: undefined,
    defaultPartitions: "2",
    defaultReplicationFactor: "2",
  }),
  createMockEnvironmentDTO({
    name: "PROD",
    id: "3",
    maxPartitions: "16",
    maxReplicationFactor: "3",
    defaultPartitions: "2",
    defaultReplicationFactor: "2",
  }),
];

const TopicAclRequest = () => {
  const { topicName = "" } = useParams();
  const navigate = useNavigate();
  const [topicType, setTopicType] = useState("Producer");

  useEffect(() => {
    if (window.msw !== undefined) {
      mockGetEnvironments({
        mswInstance: window.msw,
        response: { data: mockedData },
      });
      mockGetTopicNames({
        mswInstance: window.msw,
        response: mockedResponseTopicNames,
      });
      mockGetTopicTeam({
        mswInstance: window.msw,
        response: mockedResponseTopicTeamLiteral,
        topicName,
      });
      mockGetClusterInfoFromEnv({
        mswInstance: window.msw,
        response: getMockedResponseGetClusterInfoFromEnv(true),
      });
    }
  }, []);

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
    },
  });

  const { data: topicNames } = useQuery<TopicNames, Error>(["topic-names"], {
    queryFn: () => getTopicNames({ onlyMyTeamTopics: false }),
    keepPreviousData: true,
    onSuccess: (data) => {
      if (data?.includes(topicName)) {
        return;
      }
      // Navigate back to Topics when topicName does not exist in the topics list
      navigate("/topics");
    },
    enabled: topicName !== "",
  });

  const { data: environments } = useQuery<Environment[], Error>(
    ["topic-environments"],
    {
      queryFn: getEnvironments,
    }
  );

  const selectedPatternType =
    topicType === "Producer"
      ? topicProducerForm.watch("aclPatternType")
      : "LITERAL";
  const { data: topicTeam } = useQuery<TopicTeam, Error>({
    queryKey: ["topicTeam", topicName, selectedPatternType],
    queryFn: () =>
      getTopicTeam({ topicName, patternType: selectedPatternType }),
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
    ["cluster-info", selectedEnvironment],
    {
      queryFn: () =>
        getClusterInfo({
          envSelected: selectedEnvironment,
          envType: selectedEnvironmentType,
        }),
      keepPreviousData: false,
      enabled:
        selectedEnvironment !== ENVIRONMENT_NOT_INITIALIZED &&
        environments !== undefined &&
        selectedEnvironmentType !== undefined,
      onSuccess: (data) => {
        const isAivenCluster = data?.aivenCluster === "true";
        // Enable the only possible option when the environment chosen is Aiven Kafka flavor
        if (isAivenCluster) {
          return topicType === "Producer"
            ? topicProducerForm.setValue("aclIpPrincipleType", "PRINCIPAL")
            : topicConsumerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
        }
      },
    }
  );

  if (
    topicNames === undefined ||
    environments === undefined ||
    topicTeam === undefined
  ) {
    return <SkeletonForm />;
  }

  return (
    <Box maxWidth={"4xl"}>
      {topicType === "Consumer" ? (
        <TopicConsumerForm
          renderAclTypeField={() => (
            <AclTypeField topicType={topicType} handleChange={setTopicType} />
          )}
          topicConsumerForm={topicConsumerForm}
          topicNames={topicNames}
          topicTeam={topicTeam.team}
          environments={environments}
          clusterInfo={clusterInfo}
        />
      ) : (
        <TopicProducerForm
          renderAclTypeField={() => (
            <AclTypeField topicType={topicType} handleChange={setTopicType} />
          )}
          topicProducerForm={topicProducerForm}
          topicNames={topicNames}
          topicTeam={topicTeam.team}
          environments={environments}
          clusterInfo={clusterInfo}
        />
      )}
    </Box>
  );
};

export default TopicAclRequest;
