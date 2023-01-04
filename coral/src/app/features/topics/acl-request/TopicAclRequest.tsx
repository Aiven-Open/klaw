import { Box } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ClusterInfo,
  clusterInfoFromEnvironment,
  Environment,
  getEnvironments,
  mockGetEnvironments,
} from "src/domain/environment";
import {
  mockGetClusterInfoFromEnv,
  mockedResponseGetClusterInfoFromEnv,
} from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { TopicNames, TopicTeam } from "src/domain/topic";
import {
  mockGetTopicNames,
  mockedResponseTopicNames,
  mockGetTopicTeam,
  mockedResponseTopicTeamLiteral,
} from "src/domain/topic/topic-api.msw";
import {
  topicNamesQuery,
  topicTeamQuery,
} from "src/domain/topic/topic-queries";

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

  useEffect(() => {
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
      response: mockedResponseGetClusterInfoFromEnv,
      envSelected: "1",
      envType: "kafka",
    });
  }, []);

  const { data: topicNames } = useQuery<TopicNames, Error>(["topic-names"], {
    ...topicNamesQuery(),
    onSettled: (data) => {
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
    getEnvironments
  );
  const { data: topicTeam } = useQuery<TopicTeam, Error>(
    ["topic-team", topicName],
    topicTeamQuery({ topicName })
  );
  const envSelected = "1";
  const envType = "kafka";
  const { data: clusterInfo } = useQuery<ClusterInfo, Error>(
    ["cluster-info", envSelected, envType],
    clusterInfoFromEnvironment({ envSelected, envType })
  );

  const isLoading =
    environments === undefined ||
    topicNames === undefined ||
    topicTeam === undefined ||
    clusterInfo === undefined;

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <Box style={{ maxWidth: 600 }}>
      <div>Topic name: {topicName}</div>
      <div>Topic team: {topicTeam.team}</div>
      <div>Cluster info: {JSON.stringify(clusterInfo)}</div>
      <div>Environments:</div>
      {environments.map((env) => (
        <li key={env.id}>{env.name}</li>
      ))}
      <div>Topic names:</div>
      {topicNames.map((topicName) => (
        <li key={topicName}>{topicName}</li>
      ))}
    </Box>
  );
};

export default TopicAclRequest;
