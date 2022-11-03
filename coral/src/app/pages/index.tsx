import { useQuery } from "@tanstack/react-query";

const HomePage = () => {
  const { isLoading, data } = useQuery(["testData"], () => {
    return Promise.resolve({ data: "hello" });
  });

  return (
    <>
      <h1>Index</h1>
      {isLoading && <p>data is loading</p>}
      {!isLoading && data && <p>{data.data}</p>}
    </>
  );
};

export default HomePage;
