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
