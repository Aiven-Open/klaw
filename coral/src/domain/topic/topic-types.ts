import type { KlawApiModel } from "types/utils";

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type TopicApiResponse = Paginated<Topic[]>;

type Topic = KlawApiModel<"TopicInfo">;
type TopicNames = KlawApiModel<"TopicsGetOnlyResponse">;

export type { Topic, TopicNames, TopicApiResponse };
