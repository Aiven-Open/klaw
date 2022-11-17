import {
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topics/topics-types";

// @TODO check zod for this!
function transformTopicApiResponse(
  apiResponse: TopicDTOApiResponse
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

export { transformTopicApiResponse };
