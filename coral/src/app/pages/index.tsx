import { Pagination } from "src/app/components/Pagination";

const currentPageForTesting = 2;
const totalPagesForTesting = 5;

const HomePage = () => {
  return (
    <>
      <h1>Index</h1>
      <br />
      <br />
      <br />
      <br />
      <br />

      <Pagination
        activePage={currentPageForTesting}
        totalPages={totalPagesForTesting}
      />
    </>
  );
};

export default HomePage;
