import { useQuery } from "@tanstack/react-query";
import { getAuthUser } from "src/domain/auth-user";

const HomePage = () => {
  const { isLoading, data } = useQuery(["testData"], getAuthUser);
  return (
    <>
      <h1>Index</h1>
      {isLoading && <p>data is loading</p>}
      {!isLoading && data && <p>{data.name}</p>}
    </>
  );
};

export default HomePage;
