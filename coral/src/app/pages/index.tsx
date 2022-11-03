import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuthUser } from "src/domain/auth-user";
import { mockUserAuthRequest } from "src/domain/auth-user/auth-user-api.msw";
import { useEffect } from "react";
import { getWindowWithMswInstance } from "src/domain/api-mocks/window-msw";

const HomePage = () => {
  useEffect(() => {
    const window = getWindowWithMswInstance();
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      const mswInstance = browserEnvWorker;
      const userObject: AuthUser = {
        name: "Super Admin",
      };
      mockUserAuthRequest({ mswInstance, userObject });
    }
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
