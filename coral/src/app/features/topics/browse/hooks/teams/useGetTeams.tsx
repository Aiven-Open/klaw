import { useQuery } from "@tanstack/react-query";
import { getTeams } from "src/domain/team";

function useGetTeams() {
  return useQuery(["topic-teams"], getTeams);
}

export { useGetTeams };
