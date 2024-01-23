import { Box, Button, Grid, Skeleton, Typography } from "@aivenio/aquarium";
import refreshIcon from "@aivenio/aquarium/dist/src/icons/refresh";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { getConsumerOffsets } from "src/domain/acl/acl-api";
import { ConsumerOffsets } from "src/domain/acl/acl-types";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";

interface ConsumerOffsetsProps {
  setError: (error: string) => void;
  topicName: string;
  environment: string;
  consumerGroup?: string;
}

const parseOffsetsContent = ({
  topicPartitionId,
  currentOffset,
  endOffset,
  lag,
}: ConsumerOffsets) => {
  return `Partition ${topicPartitionId}: Current offset ${currentOffset} | End offset ${endOffset} | Lag ${lag}`;
};

const ConsumerOffsetsValues = ({
  setError,
  topicName,
  environment,
  consumerGroup,
}: ConsumerOffsetsProps) => {
  const [shouldFetch, setShouldFetch] = useState(false);
  const {
    data: offsetsData = [],
    error: offsetsError,
    isFetched: offsetsDataFetched,
    isFetching,
    refetch,
  } = useQuery<ConsumerOffsets[], HTTPError>(
    ["getConsumerOffsets", topicName, environment, consumerGroup],
    {
      queryFn: () => {
        return getConsumerOffsets({
          topicName,
          env: environment,
          consumerGroupId: consumerGroup || "",
        });
      },
      enabled: shouldFetch,
    }
  );

  useEffect(() => {
    if (offsetsError !== null) {
      setError(parseErrorMsg(offsetsError));
    }
  }, [offsetsError]);

  return (
    <Grid style={{ gridTemplateColumns: "70% 30%" }}>
      <Grid.Item>
        {!shouldFetch && (
          <Typography.Default htmlTag="dd">
            Fetch offset lag to display data.
          </Typography.Default>
        )}
        <Box.Flex flexDirection="column">
          {offsetsData.length === 0 && offsetsDataFetched && (
            <Typography.Default htmlTag="dd">
              No offset lag is currently retained.
            </Typography.Default>
          )}
          {isFetching ? (
            <Box.Flex
              flexDirection={"column"}
              gap={"2"}
              data-testid={"offsets-skeleton"}
            >
              {/* Render one skeleton on first fetch
              Render as many skeletons as partitions for refetch */}
              {(offsetsData.length === 0 ? ["skeleton"] : offsetsData).map(
                (_, index) => {
                  return <Skeleton key={index} height={22} width={200} />;
                }
              )}
            </Box.Flex>
          ) : (
            offsetsData.map((data, index) => {
              return (
                <Typography.Default
                  key={data.topicPartitionId || index}
                  htmlTag="dd"
                >
                  {parseOffsetsContent(data)}
                </Typography.Default>
              );
            })
          )}
        </Box.Flex>
      </Grid.Item>
      <Grid.Item justifySelf="right" alignSelf="end">
        <Button.Secondary
          key="button"
          onClick={() => {
            if (!shouldFetch) {
              setShouldFetch(true);
              return;
            }
            refetch();
          }}
          disabled={isFetching}
          loading={isFetching}
          aria-label={`${
            shouldFetch ? "Refetch" : "Fetch"
          } the consumer offset lag of the current subscription`}
          icon={refreshIcon}
        >
          {shouldFetch ? "Refetch" : "Fetch"} offset lag
        </Button.Secondary>
      </Grid.Item>
    </Grid>
  );
};

export { ConsumerOffsetsValues };
