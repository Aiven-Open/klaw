import type { KlawApiModel } from "types/utils";

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type TopicApiResponse = Paginated<Topic[]>;

type Topic = KlawApiModel<"TopicInfo">;

export type { Topic, TopicApiResponse };
