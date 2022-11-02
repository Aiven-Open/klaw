import { useQuery } from "@tanstack/react-query";

const HomePage = () => {
  const getAuthUser = async () => {
    const res = await fetch("/user/authenticate", {
      method: "POST",
    });
    return res.json();
  };

  const { isLoading, data } = useQuery(["testData"], getAuthUser);

  return (
    <>
      <h1>Index</h1>
      {isLoading && <p>data is loading</p>}
      {!isLoading && data && <p>{data.username}</p>}
    </>
  );
};

export default HomePage;
