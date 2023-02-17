import { PaginationBase } from "src/app/components/PaginationBase";
import { Dispatch, SetStateAction, useState } from "react";
import { useSearchParams } from "react-router-dom";

type PaginationTestProps = {
  totalPages: number | undefined;
  initialPage: number;
  setPage: Dispatch<SetStateAction<number>>;
};

function Pagination(props: PaginationTestProps) {
  const { initialPage, totalPages, setPage } = props;
  const [searchParams, setSearchParams] = useSearchParams();
  const [currentPage, setCurrentPage] = useState(
    Number(searchParams.get("page")) || initialPage
  );

  function changePage(activePage: number) {
    setPage(activePage);
    setCurrentPage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  }

  if (!totalPages || totalPages <= 1) return null;
  return (
    <PaginationBase
      activePage={currentPage}
      totalPages={totalPages}
      setActivePage={changePage}
    />
  );
}

export { Pagination };
