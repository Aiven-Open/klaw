import { PaginationBase } from "src/app/components/PaginationBase";
import { Dispatch, SetStateAction, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

type PaginationTestProps = {
  totalPages: number | undefined;
  page: number | undefined;
  setPage: Dispatch<SetStateAction<number>>;
};

function Pagination(props: PaginationTestProps) {
  const { totalPages, page, setPage } = props;

  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = searchParams.get("page");

  useEffect(() => {
    const pageToSet = initialPage ? Number(initialPage) : 1;
    setPage(pageToSet);
  }, [initialPage]);

  function changePage(activePage: number) {
    setPage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  }

  if (!totalPages || totalPages <= 1) return null;
  return (
    <PaginationBase
      activePage={page || 1}
      totalPages={totalPages}
      setActivePage={changePage}
    />
  );
}

export { Pagination };
