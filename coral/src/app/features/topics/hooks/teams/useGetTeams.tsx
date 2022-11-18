import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import { getTeams, Team } from "src/domain/team";
import { mockGetTeams } from "src/domain/topic/topic-api.msw";

function useGetTeams(): UseQueryResult<Team[]> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockGetTeams({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<Team[], Error>(["topic-teams"], () => getTeams());
}

export { useGetTeams };
