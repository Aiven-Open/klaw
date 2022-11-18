export type EnvironmentDTO = {
  id: string;
  name: string;
  type: string;
  tenantId: number;
  topicprefix: null;
  topicsuffix: null;
  clusterId: number;
  tenantName: string;
  clusterName: string;
  envStatus: string;
  otherParams: string;
  defaultPartitions: null;
  maxPartitions: null;
  defaultReplicationFactor: null;
  maxReplicationFactor: null;
  showDeleteEnv: boolean;
  totalNoPages: null;
  allPageNos: null;
};

export type Environment = string;
