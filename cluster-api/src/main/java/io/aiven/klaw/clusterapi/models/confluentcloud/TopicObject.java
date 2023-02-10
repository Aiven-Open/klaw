package io.aiven.klaw.clusterapi.models.confluentcloud;

public class TopicObject {
  public String kind;
  public Metadata metadata;
  public String cluster_id;
  public String topic_name;
  public boolean is_internal;
  public int replication_factor;
  public int partitions_count;
  public Partitions partitions;
  public Configs configs;
  public PartitionReassignments partition_reassignments;
}
