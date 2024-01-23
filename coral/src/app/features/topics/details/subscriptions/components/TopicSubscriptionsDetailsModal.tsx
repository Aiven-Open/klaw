import {
  Alert,
  Box,
  Divider,
  Grid,
  Skeleton,
  StatusChip,
  Typography,
} from "@aivenio/aquarium";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Modal } from "src/app/components/Modal";
import { ConsumerOffsetsValues } from "src/app/features/topics/details/subscriptions/components/ConsumerOffsetsValues";
import { getAivenServiceAccountDetails } from "src/domain/acl/acl-api";
import { ServiceAccountDetails } from "src/domain/acl/acl-types";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

interface TopicSubscriptionsDetailsModalProps {
  closeDetailsModal: () => void;
  isAivenCluster: boolean;
  selectedSubscription: AclOverviewInfo;
  serviceAccountData?: ServiceAccountDetails;
}

const TopicSubscriptionsDetailsModal = ({
  closeDetailsModal,
  isAivenCluster,
  selectedSubscription,
}: TopicSubscriptionsDetailsModalProps) => {
  const {
    environment,
    consumergroup,
    topicname,
    req_no,
    topictype,
    environmentName,
    acl_ssl,
    acl_ip,
    aclPatternType,
  } = selectedSubscription;

  const queryClient = useQueryClient();
  const [errors, setErrors] = useState<string[]>([]);

  const {
    data: serviceAccountData,
    error: serviceAccountError,
    isFetched: serviceAccountDataFetched,
  } = useQuery<ServiceAccountDetails, HTTPError>(
    ["getAivenServiceAccountDetails", environment, topicname, acl_ssl, req_no],
    {
      queryFn: () => {
        return getAivenServiceAccountDetails({
          env: environment,
          topicName: topicname,
          serviceName: acl_ssl || "",
          aclReqNo: req_no,
        });
      },
      // Service account data is only available for Aiven clusters
      enabled: isAivenCluster,
    }
  );

  useEffect(() => {
    if (serviceAccountError !== null) {
      setErrors((prev) => [...prev, parseErrorMsg(serviceAccountError)]);
    }
  }, [serviceAccountError]);

  const serviceAccountDataLoaded =
    serviceAccountDataFetched && serviceAccountData !== undefined;

  const serviceAccountOrPrincipalText = isAivenCluster
    ? "Service account"
    : "Principal";

  return (
    <Modal
      title={"Subscription details"}
      primaryAction={{
        text: "Close",
        onClick: () => {
          closeDetailsModal();
          queryClient.removeQueries({ queryKey: ["getConsumerOffsets"] });
        },
      }}
      close={() => {
        closeDetailsModal();
        queryClient.removeQueries({ queryKey: ["getConsumerOffsets"] });
      }}
    >
      <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
        {errors.length > 0 && (
          <Grid.Item xs={2}>
            <Alert type="error">
              <Box.Flex flexDirection={"column"}>
                {errors.map((error) => (
                  <div key={error}>{error}</div>
                ))}
              </Box.Flex>
            </Alert>
          </Grid.Item>
        )}

        <Grid.Item xs={2}>
          <Box.Flex flexDirection={"column"} width={"min"}>
            <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
              Environment
            </Typography.SmallStrong>
            <Typography.Default htmlTag="dd">
              {environmentName}
            </Typography.Default>
          </Box.Flex>
        </Grid.Item>
        <Grid.Item xs={2}>
          <Divider />
        </Grid.Item>
        <Box.Flex flexDirection={"column"}>
          <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
            Subscription type
          </Typography.SmallStrong>
          <Typography.Default htmlTag="dd">
            <StatusChip
              status={topictype === "Consumer" ? "success" : "info"}
              text={topictype.toUpperCase()}
            />
          </Typography.Default>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
            Pattern type
          </Typography.SmallStrong>
          <Typography.Default htmlTag="dd">{aclPatternType}</Typography.Default>
        </Box.Flex>
        <Grid.Item xs={2}>
          <Divider />
        </Grid.Item>
        <Box.Flex flexDirection={"column"} width={"min"}>
          <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
            Topic name
          </Typography.SmallStrong>
          <Typography.Default htmlTag="dd">{topicname}</Typography.Default>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
            Consumer group
          </Typography.SmallStrong>
          <Typography.Default htmlTag="dd">{consumergroup}</Typography.Default>
        </Box.Flex>

        <Box.Flex flexDirection={"column"}>
          <Typography.SmallStrong
            htmlTag={"dt"}
            color={"grey-60"}
          >{`IP or ${serviceAccountOrPrincipalText} based`}</Typography.SmallStrong>
          <Typography.Default htmlTag="dd">
            {acl_ip === undefined ? serviceAccountOrPrincipalText : "IP"}
          </Typography.Default>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
            {acl_ip === undefined ? serviceAccountOrPrincipalText : "IP"}
          </Typography.SmallStrong>
          <Typography.Default htmlTag="dd">
            <StatusChip
              status={"neutral"}
              text={acl_ip || acl_ssl || "Not found"}
            />
          </Typography.Default>
        </Box.Flex>
        {isAivenCluster ? (
          <Grid.Item xs={2}>
            <Box.Flex flexDirection={"column"}>
              <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
                Service account password
              </Typography.SmallStrong>
              {serviceAccountDataLoaded ? (
                <Typography.Default htmlTag="dd">
                  {serviceAccountData.password || "Not authorized to see this."}
                </Typography.Default>
              ) : (
                <div data-testid={"pw-skeleton"}>
                  <Skeleton height={25} width={250} />
                </div>
              )}
            </Box.Flex>
          </Grid.Item>
        ) : null}
        {!isAivenCluster && selectedSubscription.topictype === "Consumer" ? (
          <Grid.Item xs={2}>
            <Box.Flex flexDirection={"column"}>
              <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
                Consumer offset lag
              </Typography.SmallStrong>
              <ConsumerOffsetsValues
                setError={(error: string) =>
                  setErrors((prev) => [...prev, error])
                }
                topicName={topicname}
                environment={environment}
                consumerGroup={consumergroup}
              />
            </Box.Flex>
          </Grid.Item>
        ) : null}
      </Grid>
    </Modal>
  );
};

export default TopicSubscriptionsDetailsModal;
