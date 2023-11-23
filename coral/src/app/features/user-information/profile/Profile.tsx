import { useQuery } from "@tanstack/react-query";
import { getUser } from "src/domain/user/user-api";

function Profile() {
  const { data: user } = useQuery({
    queryKey: ["getUser"],
    queryFn: getUser,
  });
  return <div>This is profile page of {user?.username}</div>;
}

export { Profile };
