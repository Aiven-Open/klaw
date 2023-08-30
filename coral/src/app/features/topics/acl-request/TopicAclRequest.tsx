import { Box } from "@aivenio/aquarium";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-consumer";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-producer";
import { environment } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import SkeletonForm from "src/app/features/topics/acl-request/forms/SkeletonForm";
import TopicConsumerForm from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import TopicProducerForm from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import useExtendedEnvironments from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";
import { AclType } from "src/domain/acl";

const TopicAclRequest = () => {
  const navigate = useNavigate();
  const { topicName } = useParams();
  const [searchParams] = useSearchParams();

  const [aclType, setAclType] = useState<AclType>("PRODUCER");

  const topicProducerForm = useForm<TopicProducerFormSchema>({
    schema: topicProducerFormSchema,
    defaultValues: {
      topicname: topicName,
      aclType: "PRODUCER",
      aclPatternType: topicName !== undefined ? "LITERAL" : undefined,
      environment: searchParams.get("env") ?? undefined,
      // teamId is required, but we only add it to the form data when submitting the form
      // as it depends on the topicname and aclpatterntype fiewlds
      teamId: 0,
    },
  });

  const topicConsumerForm = useForm<TopicConsumerFormSchema>({
    schema: topicConsumerFormSchema,
    defaultValues: {
      aclPatternType: "LITERAL",
      topicname: topicName,
      aclType: "CONSUMER",
      consumergroup: "",
      environment: searchParams.get("env") ?? undefined,
      // teamId is required, but we only add it to the form data when submitting the form...
      // ...as it depends on the topicname and aclPatternType fields.
      // We need to give it a default value, otherwise the submit handler of the Form would not trigger...
      /// ... because the form data would be missing a required value.
      teamId: 0,
    },
  });

  const { extendedEnvironments, hasFetchedExtendedEnvironments } =
    useExtendedEnvironments();

  const currentEnv = searchParams.get("env");
  const isValidEnv =
    extendedEnvironments.find((env) => currentEnv === env.id) !== undefined;

  // /topic/aivendemotopic/subscribe route requires an env search param to function correctly
  // So we redirect when it is missing
  if (
    hasFetchedExtendedEnvironments &&
    topicName !== undefined &&
    !isValidEnv
  ) {
    navigate(`/topic/${topicName}/subscriptions`);
  }

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
      extendedEnvironments,
      aclType,
      topicProducerForm.watch("environment"),
      topicConsumerForm.watch("environment"),
    ]
  );

  // If the environment selected is an Aiven cluster, some fields can only have a certain value,
  // so we need to set those values when a user selects an environment which is an Aiven cluster
  useEffect(() => {
    if (
      selectedEnvironment !== undefined &&
      selectedEnvironment.isAivenCluster
    ) {
      if (aclType === "PRODUCER") {
        topicProducerForm.setValue("aclPatternType", "LITERAL", {
          shouldValidate: true,
        });
        topicProducerForm.setValue("aclIpPrincipleType", "PRINCIPAL", {
          shouldValidate: true,
        });
        topicProducerForm.resetField("transactionalId");
      }
      if (aclType === "CONSUMER") {
        topicConsumerForm.setValue("aclIpPrincipleType", "PRINCIPAL", {
          shouldValidate: true,
        });
      }
    }
  }, [selectedEnvironment, aclType]);

  if (!hasFetchedExtendedEnvironments) {
    return <SkeletonForm />;
  }

  const currentTopicNames =
    selectedEnvironment === undefined && topicName !== undefined
      ? [topicName]
      : selectedEnvironment?.topicNames;

  return (
    <Box>
      {aclType === "CONSUMER" ? (
        <TopicConsumerForm
          renderAclTypeField={() => (
            <AclTypeField aclType={aclType} handleChange={setAclType} />
          )}
          topicConsumerForm={topicConsumerForm}
          topicNames={currentTopicNames || []}
          environments={extendedEnvironments}
          isAivenCluster={selectedEnvironment?.isAivenCluster}
          // This prop is true when user navigated to /topic/{topicName}/subscribe?env={id}
          // False when navigating to /request/acl
          isSubscription={topicName !== undefined && environment !== undefined}
        />
      ) : (
        <TopicProducerForm
          renderAclTypeField={() => (
            <AclTypeField aclType={aclType} handleChange={setAclType} />
          )}
          topicProducerForm={topicProducerForm}
          topicNames={currentTopicNames || []}
          environments={extendedEnvironments}
          isAivenCluster={selectedEnvironment?.isAivenCluster}
          // This prop is true when user navigated to /topic/{topicName}/subscribe?env={id}
          // False when navigating to /request/acl
          isSubscription={topicName !== undefined && environment !== undefined}
        />
      )}
    </Box>
  );
};

export default TopicAclRequest;
