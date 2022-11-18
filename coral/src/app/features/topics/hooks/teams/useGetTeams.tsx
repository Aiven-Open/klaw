import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import { mockGetTeams } from "src/domain/topic/topic-api.msw";
import { getTeams } from "src/domain/topic";
import { TopicTeams } from "src/domain/topic/topic-types";

function useGetTeams(): UseQueryResult<TopicTeams> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockGetTeams({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<TopicTeams, Error>(["topic-teams"], () => getTeams());
}

export { useGetTeams };
