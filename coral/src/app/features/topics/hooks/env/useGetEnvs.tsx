import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import { mockGetEnvs } from "src/domain/topics/topics-api.msw";
import { TopicEnv } from "src/domain/topics";
import { getEnvs } from "src/domain/topics";

function useGetEnvs(): UseQueryResult<TopicEnv[]> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockGetEnvs({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<TopicEnv[], Error>(["topic-envs"], () => getEnvs());
}

export { useGetEnvs };
