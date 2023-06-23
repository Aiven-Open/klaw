import {
  Alert,
  Box,
  Divider,
  Grid,
  GridItem,
  Skeleton,
  StatusChip,
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
  selectedSub: AclOverviewInfo;
  offsetsData?: ConsumerOffsets;
  serviceAccountData?: ServiceAccountDetails;
}

const Label = ({ children }: { children: React.ReactNode }) => (
  <dt className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </dt>
);

const TopicSubscriptionsDetailsModal = ({
  closeDetailsModal,
  isAivenCluster,
  selectedSub,
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
  } = selectedSub;

  const [errors, setErrors] = useState<string[]>([]);

  const {
    data: offsetsData,
    error: offsetsError,
    isFetched: offsetsDataFetched,
  } = useQuery<ConsumerOffsets[] | undefined, HTTPError>(
    ["consumer-offsets", topicname, environment, consumergroup],
    {
      queryFn: () => {
        if (consumergroup !== undefined) {
          return getConsumerOffsets({
            topicName: topicname,
            env: environment,
            consumerGroupId: consumergroup,
          });
        }
      },
      // Offsets data is only available for Consumer subscriptions in non-Aiven clusters
      enabled: topictype === "Consumer" && !isAivenCluster,
    }
  );

  const {
    data: serviceAccountData,
    error: serviceAccountError,
    isFetched: serviceAccountDataFetched,
  } = useQuery<ServiceAccountDetails | undefined, HTTPError>(
    [
      "getAivenServiceAccountDetails",
      environment,
      topicname,
      user?.username,
      req_no,
    ],
    {
      queryFn: () => {
        if (
          environment !== undefined &&
          req_no !== undefined &&
          user?.username !== undefined
        ) {
          return getAivenServiceAccountDetails({
            env: environment,
            topicName: topicname,
            userName: user.username,
            aclReqNo: req_no,
          });
        }
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
            <Label>Environment</Label>
            <dd>{environmentName}</dd>
          </Box.Flex>
        </GridItem>
        <GridItem colSpan={"span-2"}>
          <Divider />
        </GridItem>
        <Box.Flex flexDirection={"column"}>
          <Label>Subscription type</Label>
          <dd>
            <StatusChip
              status={topictype === "Consumer" ? "success" : "info"}
              text={topictype.toUpperCase()}
            />
          </dd>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Label>Pattern type</Label>
          <dd>{aclPatternType}</dd>
        </Box.Flex>
        <GridItem colSpan={"span-2"}>
          <Divider />
        </GridItem>
        <Box.Flex flexDirection={"column"} width={"min"}>
          <Label>Topic name</Label>
          <dd>{topicname}</dd>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Label>Consumer group</Label>
          <dd>{consumergroup}</dd>
        </Box.Flex>

        <Box.Flex flexDirection={"column"}>
          <Label>{`IP or ${serviceAccountOrPrincipalText} based`}</Label>
          <dd>{acl_ip === undefined ? serviceAccountOrPrincipalText : "IP"}</dd>
        </Box.Flex>
        <Box.Flex flexDirection={"column"}>
          <Label>
            {acl_ip === undefined ? serviceAccountOrPrincipalText : "IP"}
          </Label>
          <dd>
            <StatusChip
              status={"neutral"}
              text={acl_ip || acl_ssl || "Not found"}
            />
          </dd>
        </Box.Flex>
        {isAivenCluster ? (
          <GridItem colSpan={"span-2"}>
            <Box.Flex flexDirection={"column"}>
              <Label>Service account password</Label>
              {serviceAccountDataLoaded ? (
                serviceAccountData.password
              ) : (
                <Skeleton height={25} width={250} />
              )}
            </Box.Flex>
          </GridItem>
        ) : null}
        {!isAivenCluster && selectedSub.topictype === "Consumer" ? (
          <GridItem colSpan={"span-2"}>
            <Box.Flex flexDirection={"column"} col>
              <Label>Consumer offset</Label>
              {offsetsDataLoaded ? (
                `Partition ${offsetsData[0].topicPartitionId} | Current offset
                ${offsetsData[0].currentOffset} | End offset
                ${offsetsData[0].endOffset} | Lag ${offsetsData[0].lag}`
              ) : (
                <Skeleton height={25} width={350} />
              )}
            </Box.Flex>
          </GridItem>
        ) : null}
      </Grid>
    </Modal>
  );
};

export default TopicSubscriptionsDetailsModal;
