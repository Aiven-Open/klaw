// ************************
// Types related to TOPIC
//*************************

export type TopicDTO = {
  topicid: number;
  sequence: string;
  totalNoPages: string;
  currentPage: string;
  allPageNos: string[];
  topicName: string;
  noOfPartitions: number;
  description: string;
  documentation: string | null;
  noOfReplcias: string;
  teamname: string;
  cluster: string;
  clusterId: number | null;
  environmentsList: string[];
  showEditTopic: boolean;
  showDeleteTopic: boolean;
  topicDeletable: boolean;
};

export type TopicApiResponse = {
  totalPages: number;
  currentPage: number;
  entries: Topic[];
};

// @TODO adjust to our needs
export type Topic = TopicDTO;
export type TopicDTOApiResponse = Array<Array<TopicDTO>>;

// ****************************
// Types related to topic ENV
//*****************************

export type TopicEnvDTO = {
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

export const enum TopicEnv {
  ALL = "ALL",
  DEV = "DEV",
  TST = "TST",
}

// ****************************
// Types related to topic ENV
//*****************************

export type TopicTeams = string[];
