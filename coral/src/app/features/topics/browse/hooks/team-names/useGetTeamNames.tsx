import { useQuery } from "@tanstack/react-query";
import { getTeamNames } from "src/domain/team";

function useGetTeamNames() {
  return useQuery(["topic-team-names"], getTeamNames);
}

export { useGetTeamNames };
