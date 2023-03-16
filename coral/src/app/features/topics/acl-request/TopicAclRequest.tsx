import { Box } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import SkeletonForm from "src/app/features/topics/acl-request/forms/SkeletonForm";
import TopicConsumerForm from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import TopicProducerForm from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import useExtendedEnvironments from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
import { getTopicTeam, TopicTeam } from "src/domain/topic";
import { AclType } from "src/domain/acl";

const TopicAclRequest = () => {
  const { topicName } = useParams();
  const [aclType, setAclType] = useState<AclType>("PRODUCER");

  const topicProducerForm = useForm<TopicProducerFormSchema>({
    schema: topicProducerFormSchema,
    defaultValues: {
      topicname: topicName,
      aclType: "PRODUCER",
    },
  });

  const topicConsumerForm = useForm<TopicConsumerFormSchema>({
    schema: topicConsumerFormSchema,
    defaultValues: {
      aclPatternType: "LITERAL",
      topicname: topicName,
      aclType: "CONSUMER",
      consumergroup: "",
    },
  });

  const { isLoadingExtendedEnvironments, extendedEnvironments } =
    useExtendedEnvironments();

  // Will trigger infinite rerender when selecting an environment if not memoized
  const selectedEnvironment = useMemo(
    () =>
      aclType === "PRODUCER"
        ? extendedEnvironments.find(
            (env) => env.id === topicProducerForm.watch("environment")
          )
        : extendedEnvironments.find(
            (env) => env.id === topicConsumerForm.watch("environment")
          ),
    [
      aclType,
      topicProducerForm.watch("environment"),
      topicConsumerForm.watch("environment"),
    ]
  );

  const selectedPatternType =
    aclType === "PRODUCER"
      ? topicProducerForm.watch("aclPatternType")
      : "LITERAL";
  const selectedTopicName =
    aclType === "PRODUCER"
      ? topicProducerForm.watch("topicname")
      : topicConsumerForm.watch("topicname");
  useQuery<TopicTeam, Error>(
    ["topicTeam", selectedTopicName, selectedPatternType, aclType],
    {
      queryFn: () =>
        getTopicTeam({
          topicName: selectedTopicName,
          patternType: selectedPatternType,
        }),
      onSuccess: ({ error, team }) => {
        // If error is not undefined, the other properties will be undefined
        // Then it means that the topic and pattern type the user has chosen are incompatible
        // And therefore no single team can be returned
        // Example: pattern type is PREFIXED, but different topics with the prefix have different teams
        // We therefore need to error
        // @TODO this should be an error notification, not a runtime error
        if (error !== undefined || team === undefined) {
          throw new Error(error);
        }
        return aclType === "PRODUCER"
          ? topicProducerForm.setValue("teamname", team)
          : topicConsumerForm.setValue("teamname", team);
      },
      enabled: selectedTopicName !== undefined,
      keepPreviousData: true,
    }
  );

  // If the environment selected is an Aiven cluster, some fields can only have a certain value,
  // so we need to set those values when a user selects an environment which is an Aiven cluster
  useEffect(() => {
    if (
      selectedEnvironment !== undefined &&
      selectedEnvironment.isAivenCluster
    ) {
      if (aclType === "PRODUCER") {
        topicProducerForm.setValue("aclPatternType", "LITERAL");
        topicProducerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
        topicProducerForm.resetField("transactionalId");
      }
      if (aclType === "CONSUMER") {
        topicConsumerForm.setValue("aclIpPrincipleType", "PRINCIPAL");
      }
    }
  }, [selectedEnvironment]);

  if (isLoadingExtendedEnvironments) {
    return <SkeletonForm />;
  }

  const currentTopicNames = selectedEnvironment?.topicNames || [];

  return (
    <Box maxWidth={"7xl"}>
      {aclType === "CONSUMER" ? (
        <TopicConsumerForm
          renderAclTypeField={() => (
            <AclTypeField aclType={aclType} handleChange={setAclType} />
          )}
          topicConsumerForm={topicConsumerForm}
          topicNames={currentTopicNames}
          environments={extendedEnvironments}
          isAivenCluster={selectedEnvironment?.isAivenCluster}
        />
      ) : (
        <TopicProducerForm
          renderAclTypeField={() => (
            <AclTypeField aclType={aclType} handleChange={setAclType} />
          )}
          topicProducerForm={topicProducerForm}
          topicNames={currentTopicNames}
          environments={extendedEnvironments}
          isAivenCluster={selectedEnvironment?.isAivenCluster}
        />
      )}
    </Box>
  );
};

export default TopicAclRequest;
