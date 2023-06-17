import { TopicOverview } from "src/domain/topic";

const getTopicStats = (data: TopicOverview) => {
  const aclInfoList = data?.aclInfoList ?? [];
  const prefixedAclInfoList = data?.prefixedAclInfoList ?? [];
  const transactionalAclInfoList = data?.transactionalAclInfoList ?? [];

  return [
    ...aclInfoList,
    ...prefixedAclInfoList,
    ...transactionalAclInfoList,
  ].reduce(
    (previous, current) => {
      const nextProducers =
        current.topictype === "Producer"
          ? previous.producers + 1
          : previous.producers;
      const nextConsumers =
        current.topictype === "Consumer"
          ? previous.consumers + 1
          : previous.consumers;

      return {
        ...previous,
        producers: nextProducers,
        consumers: nextConsumers,
      };
    },
    {
      producers: 0,
      consumers: 0,
      partitions: data.topicInfo.noOfPartitions,
      // @TODO: noOfReplicas is a string, probably should be a number
      replicas: Number(data.topicInfo.noOfReplicas),
    }
  );
};

export { getTopicStats };
