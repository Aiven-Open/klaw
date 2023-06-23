import {
  Alert,
  Box,
  Divider,
  Grid,
  GridItem,
  Skeleton,
  StatusChip,
  Typography,
} from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Modal } from "src/app/components/Modal";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import {
  getAivenServiceAccountDetails,
  getConsumerOffsets,
} from "src/domain/acl/acl-api";
import {
  ConsumerOffsets,
  ServiceAccountDetails,
} from "src/domain/acl/acl-types";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

interface TopicSubscriptionsDetailsModalProps {
  closeDetailsModal: () => void;
  isAivenCluster: boolean;
  selectedSubscription: AclOverviewInfo;
  offsetsData?: ConsumerOffsets;
  serviceAccountData?: ServiceAccountDetails;
}

const TopicSubscriptionsDetailsModal = ({
  closeDetailsModal,
  isAivenCluster,
  selectedSubscription,
}: TopicSubscriptionsDetailsModalProps) => {
  const user = useAuthContext();
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

  const [errors, setErrors] = useState<string[]>([]);

  const {
    data: offsetsData,
    error: offsetsError,
    isFetched: offsetsDataFetched,
  } = useQuery<ConsumerOffsets[], HTTPError>(
    ["consumer-offsets", topicname, environment, consumergroup],
    {
      queryFn: () => {
        return getConsumerOffsets({
          topicName: topicname,
          env: environment,
          consumerGroupId: consumergroup || "",
        });
      },
      // Offsets data is only available for Consumer subscriptions in non-Aiven clusters
      enabled: topictype === "Consumer" && !isAivenCluster,
    }
  );

  const {
    data: serviceAccountData,
    error: serviceAccountError,
    isFetched: serviceAccountDataFetched,
  } = useQuery<ServiceAccountDetails, HTTPError>(
    [
      "getAivenServiceAccountDetails",
      environment,
      topicname,
      user?.username,
      req_no,
    ],
    {
      queryFn: () => {
        return getAivenServiceAccountDetails({
          env: environment,
          topicName: topicname,
          userName: user?.username || "",
          aclReqNo: req_no,
        });
      },
      // Service accout data is only available for Aiven clusters
      enabled: isAivenCluster,
    }
  );

  useEffect(() => {
    if (offsetsError !== null) {
      setErrors((prev) => [...prev, parseErrorMsg(offsetsError)]);
    }
    if (serviceAccountError !== null) {
      setErrors((prev) => [...prev, parseErrorMsg(serviceAccountError)]);
    }
  }, [offsetsError, serviceAccountError]);

  const serviceAccountDataLoaded =
    serviceAccountDataFetched && serviceAccountData !== undefined;

  const offsetsDataLoaded = offsetsDataFetched && offsetsData !== undefined;

  const serviceAccountOrPrincipalText = isAivenCluster
    ? "Service account"
    : "Principal";

  return (
    <Modal
      title={"Subscription details"}
      primaryAction={{
        text: "Close",
        onClick: closeDetailsModal,
      }}
      close={closeDetailsModal}
    >
      <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
        {errors.length > 0 && (
          <GridItem colSpan={"span-2"}>
            <Alert type="error">
              <Box.Flex flexDirection={"column"}>
                {errors.map((error) => (
                  <div key={error}>{error}</div>
                ))}
              </Box.Flex>
            </Alert>
          </GridItem>
        )}

        <GridItem colSpan={"span-2"}>
          <Box.Flex flexDirection={"column"} width={"min"}>
            <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
              Environment
            </Typography.SmallStrong>
            <Typography.Default htmlTag="dd">
              {environmentName}
            </Typography.Default>
          </Box.Flex>
        </GridItem>
        <GridItem colSpan={"span-2"}>
          <Divider />
        </GridItem>
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
        <GridItem colSpan={"span-2"}>
          <Divider />
        </GridItem>
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
          <GridItem colSpan={"span-2"}>
            <Box.Flex flexDirection={"column"}>
              <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
                Service account password
              </Typography.SmallStrong>
              {serviceAccountDataLoaded ? (
                <Typography.Default htmlTag="dd">
                  {serviceAccountData.password}
                </Typography.Default>
              ) : (
                <div data-testid={"pw-skeleton"}>
                  <Skeleton height={25} width={250} />
                </div>
              )}
            </Box.Flex>
          </GridItem>
        ) : null}
        {!isAivenCluster && selectedSubscription.topictype === "Consumer" ? (
          <GridItem colSpan={"span-2"}>
            <Box.Flex flexDirection={"column"}>
              <Typography.SmallStrong htmlTag={"dt"} color={"grey-60"}>
                Consumer offset
              </Typography.SmallStrong>
              {offsetsDataLoaded ? (
                <Typography.Default htmlTag="dd">
                  {`Partition ${offsetsData[0].topicPartitionId} |
                  Current offset ${offsetsData[0].currentOffset} |
                  End offset ${offsetsData[0].endOffset} |
                  Lag ${offsetsData[0].lag}`}
                </Typography.Default>
              ) : (
                <div data-testid={"offsets-skeleton"}>
                  <Skeleton height={25} width={350} />
                </div>
              )}
            </Box.Flex>
          </GridItem>
        ) : null}
      </Grid>
    </Modal>
  );
};

export default TopicSubscriptionsDetailsModal;
