import {
  TopicAdvancedConfigurationOptions,
  TopicApiResponse,
} from "src/domain/topic/topic-types";
import { KlawApiResponse } from "types/utils";

// @TODO check zod for this!
function transformTopicApiResponse(
  apiResponse: KlawApiResponse<"topicsGet">
): TopicApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0][0].totalNoPages),
    currentPage: Number(apiResponse[0][0].currentPage),
    entries: apiResponse.flat(),
  };
}

function transformGetTopicAdvanvedConfigOptionsResponse(
  apiResponse: KlawApiResponse<"topicAdvancedConfigGet">
): TopicAdvancedConfigurationOptions[] {
  return Object.entries(apiResponse).map(([key, name]) => ({ key, name }));
}

export {
  transformTopicApiResponse,
  transformGetTopicAdvanvedConfigOptionsResponse,
};
