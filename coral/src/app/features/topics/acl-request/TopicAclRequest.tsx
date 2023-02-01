import { Box } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import SkeletonForm from "src/app/features/topics/acl-request/forms/SkeletonForm";
import TopicConsumerForm from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import TopicProducerForm from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import useEnvironmentTopics from "src/app/features/topics/acl-request/queries/useEnvironmentTopics";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";
import { getTopicTeam, TopicTeam } from "src/domain/topic";

const TopicAclRequest = () => {
  const { topicName } = useParams();
  const [topicType, setTopicType] = useState("Producer");

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

  const {
    scopedTopicNames,
    scopedTopicNamesIsLoading,
    environmentsIsLoading,
    validEnvironments,
    clusterInfoIsLoading,
  } = useEnvironmentTopics();

  const selectedPatternType =
    topicType === "Producer"
      ? topicProducerForm.watch("aclPatternType")
      : "LITERAL";
  const selectedTopicName =
    topicType === "Producer"
      ? topicProducerForm.watch("topicname")
      : topicConsumerForm.watch("topicname");
  useQuery<TopicTeam, Error>({
    queryKey: ["topicTeam", selectedTopicName, selectedPatternType, topicType],
    queryFn: () =>
      getTopicTeam({
        topicName: selectedTopicName,
        patternType: selectedPatternType,
      }),
    onSuccess: (data) => {
      if (data === undefined) {
        throw new Error("Could not fetch team for current Topic");
      }
      return topicType === "Producer"
        ? topicProducerForm.setValue("teamname", data.team)
        : topicConsumerForm.setValue("teamname", data.team);
    },
    enabled: selectedTopicName !== undefined,
    keepPreviousData: true,
  });

  const selectedEnvironment =
    topicType === "Producer"
      ? validEnvironments.find(
          (env) => env.id === topicProducerForm.watch("environment")
        )
      : validEnvironments.find(
          (env) => env.id === topicConsumerForm.watch("environment")
        );

  useEffect(() => {
    if (
      selectedEnvironment !== undefined &&
      selectedEnvironment.isAivenCluster
    ) {
      if (topicType === "Producer") {
        topicProducerForm.setValue("aclPatternType", "LITERAL");
        topicProducerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
        topicProducerForm.resetField("transactionalId");
      }
      if (topicType === "Consumer") {
        topicConsumerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
      }
    }
  }, [selectedEnvironment]);

  if (
    environmentsIsLoading ||
    scopedTopicNamesIsLoading ||
    clusterInfoIsLoading
  ) {
    return <SkeletonForm />;
  }

  const currentTopicNames =
    scopedTopicNames.find(
      (scoped) => scoped.environmentId === selectedEnvironment?.id
    )?.topicNames || [];

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
          isAivenCluster={selectedEnvironment?.isAivenCluster}
        />
      ) : (
        <TopicProducerForm
          renderAclTypeField={() => (
            <AclTypeField topicType={topicType} handleChange={setTopicType} />
          )}
          topicProducerForm={topicProducerForm}
          topicNames={currentTopicNames}
          environments={validEnvironments}
          isAivenCluster={selectedEnvironment?.isAivenCluster}
        />
      )}
    </Box>
  );
};

export default TopicAclRequest;
