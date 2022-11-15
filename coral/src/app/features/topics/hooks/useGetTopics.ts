import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { mockTopicGetRequest } from "src/domain/topics/topics-api.msw";
import { getTopics, Topic } from "src/domain/topics";

function useGetTopics() {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockTopicGetRequest({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<Topic[], Error>(["topics"], () => {
    return getTopics();
  });
}

export { useGetTopics };
