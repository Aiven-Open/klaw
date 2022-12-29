import type { KlawApiModel } from "types/utils";

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type TopicApiResponse = Paginated<Topic[]>;

type Topic = KlawApiModel<"TopicInfo">;
type TopicNames = KlawApiModel<"TopicsGetOnlyResponse">;
type TopicTeam = KlawApiModel<"TopicGetTeamResponse">;

export type { Topic, TopicNames, TopicTeam, TopicApiResponse };
