import { useQuery } from "@tanstack/react-query";
import { getTeamNames } from "src/domain/team";

function useGetTeams() {
  return useQuery(["topic-teams"], getTeamNames);
}

export { useGetTeams };
