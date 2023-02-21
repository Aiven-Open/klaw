import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { Pagination } from "src/app/components/Pagination";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { useQuery } from "@tanstack/react-query";
import { getTopicRequestsForApprover } from "src/domain/topic/topic-api";
import { useSearchParams } from "react-router-dom";

function TopicApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();

  const initialPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const {
    data: topicRequests,
    isLoading: topicRequestsLoading,
    isError: topicRequestsIsError,
    error: topicRequestsError,
  } = useQuery({
    queryKey: ["topicRequestsForApprover", initialPage],
    queryFn: () =>
      getTopicRequestsForApprover({
        requestStatus: "ALL",
        pageNumber: initialPage,
      }),
    keepPreviousData: true,
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const filters = [
    <NativeSelect labelText={"Filter by team"} key={"filter-team"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,

    <NativeSelect
      labelText={"Filter by Environment"}
      key={"filter-environments"}
    >
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,

    <NativeSelect labelText={"Filter by status"} key={"filter-status"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={"Search for Topic..."}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Press &quot;Enter&quot; to start your search. Press &quot;Escape&quot;
        to delete all your input.
      </div>
    </div>,
  ];

  const table = <TopicApprovalsTable requests={topicRequests?.entries || []} />;
  const pagination =
    topicRequests?.totalPages && topicRequests.totalPages > 1 ? (
      <Pagination
        activePage={topicRequests.currentPage}
        totalPages={topicRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  return (
    <ApprovalsLayout
      filters={filters}
      table={table}
      pagination={pagination}
      isLoading={topicRequestsLoading}
      isErrorLoading={topicRequestsIsError}
      errorMessage={topicRequestsError}
    />
  );
}

export default TopicApprovals;
