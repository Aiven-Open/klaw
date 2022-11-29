import { components } from "types/api";

type TopicDTO = components["schemas"]["TopicInfo"];

type TopicApiResponse = {
  totalPages: number;
  currentPage: number;
  entries: Topic[];
};

type Topic = TopicDTO;
type TopicDTOApiResponse = components["schemas"]["TopicsGetResponse"];

export type { TopicDTO, TopicApiResponse, Topic, TopicDTOApiResponse };
