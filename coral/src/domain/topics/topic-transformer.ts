import { Topic, TopicDTOApiResponse } from "src/domain/topics/topics-types";

// @TODO check zod for this!
function transformTopicApiResponse(apiResponse: TopicDTOApiResponse): Topic[] {
  return apiResponse.flat();
}

export { transformTopicApiResponse };
