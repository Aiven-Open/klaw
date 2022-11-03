import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuthUser } from "src/domain/auth-user";
import { mockUserAuthRequest } from "src/domain/auth-user/auth-user-api.msw";
import { useEffect } from "react";

const HomePage = () => {
  useEffect(() => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    const mswInstance = window.msw;
    const userObject: AuthUser = {
      name: "Super Admin",
    };
    mockUserAuthRequest({ mswInstance, userObject });
  }, []);

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
