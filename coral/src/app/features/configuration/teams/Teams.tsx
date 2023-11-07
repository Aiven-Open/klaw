import { useQuery } from "@tanstack/react-query";
import { getTeams } from "src/domain/team";

function Teams() {
  const { data: teams } = useQuery(["get-teams"], {
    queryFn: () => getTeams(),
  });

  console.log(teams);
  return <div>Hello</div>;
}

export { Teams };
