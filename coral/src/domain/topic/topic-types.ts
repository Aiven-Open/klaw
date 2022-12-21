import type { KlawApiResponse, KlawApiModel } from "types/utils";

type TopicDTO = KlawApiModel<"TopicInfo">;

type TopicApiResponse = {
  totalPages: number;
  currentPage: number;
  entries: Topic[];
};

type Topic = TopicDTO;
type TopicDTOApiResponse = KlawApiResponse<"topicsGet">;

export type { TopicDTO, TopicApiResponse, Topic, TopicDTOApiResponse };
