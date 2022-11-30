import api from "src/services/api";
import {
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topic/topic-types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { Team } from "src/domain/team";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";
import isString from "lodash/isString";

const getTopics = async ({
  currentPage = 1,
  environment = "ALL",
  teamName,
  searchTerm,
}: {
  currentPage: number;
  environment: string;
  teamName: Team;
  searchTerm?: string;
}): Promise<TopicApiResponse> => {
  // "ALL_TEAMS_VALUE" represents topic list without
  // the optional team parameter
  // where we still need a way to represent an
  // option for "Select all teams" to users
  const team = isString(teamName) && teamName !== ALL_TEAMS_VALUE && teamName;

  const params: Record<string, string> = {
    pageNo: currentPage.toString(),
    env: environment,
    ...(team && { teamName: team }),
    ...(searchTerm && { topicnamesearch: searchTerm }),
  };

  return api
    .get<TopicDTOApiResponse>(`/getTopics?${new URLSearchParams(params)}`)
    .then(transformTopicApiResponse);
};

export { getTopics };
