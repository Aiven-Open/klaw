import { useQuery } from "@tanstack/react-query";
import React from "react";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import TopicTable from "src/app/features/topics/browse/components/TopicTable";

// Pure UI
// interface BrowseTopicsProps<Columns, Rows> {
//   pagination: ListPaginationProps,
//   data: {columns: Columns[], rows: Rows[]}
//   filters?: React.ReactNode[]
// }

// Reimplementation of the v6 useSearchParams in v5 API
// https://codesandbox.io/s/react-router-query-parameters-forked-h5xqcn?file=/example.js:616-1035
function useSearchParams() {
  // eslint-disable-next-line
  // @ts-ignore
  const { search } = useLocation();
  // eslint-disable-next-line
  // @ts-ignore
  const history = useHistory();

  const searchParams = new URLSearchParams(search);
  const setSearchParams = ({
    name,
    value,
  }: {
    name: string;
    value: string;
  }) => {
    searchParams.set(name, value);
    const newSearch = searchParams.toString();
    history.push({
      pathname: location.pathname,
      search: `?${newSearch}`,
    });
  };

  return [searchParams, setSearchParams];
}

// With integrated data fetching and filters handling
interface BrowseTopicsProps {
  // Unsure how tables are paginated in console, and if it is linked to search params
  pagination: React.ReactElement;
  // Use reimplementation above
  useSearchParams: () => [
    URLSearchParams,
    (name: string, value: string) => void,
  ];
  // Using react query under the hood, may work with passing consoleOrganizationKafkaTopicList from aivenApiClient/api.ts ?
  getTopics: (params: {
    organizationId: string;
    // eslint-disable-next-line
    // @ts-ignore
    options?: AxiosRequestConfig;
    // eslint-disable-next-line
    // @ts-ignore
  }) => AxiosPromise<TopicCatalogPagedResponse>;
  // Probably can be implemented as console sees fit, no need for the Klaw context?
  filters: React.ReactElement[];
}

export function BrowseTopics({
  pagination,
  useSearchParams,
  getTopics,
  filters,
}: BrowseTopicsProps) {
  // useSearchParams is used to reflect page number and filter values in URL
  // If not desirable, can be removed ?
  // eslint-disable-next-line
  // @ts-ignore
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const {
    data: topics,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["browseTopics", currentPage],
    queryFn: () =>
      getTopics({
        // Unsure how pagination works in console API calls
        // eslint-disable-next-line
        // @ts-ignore
        pageNo: currentPage.toString(),
        organizationId: "1",
      }),
    keepPreviousData: true,
  });

  return (
    <TableLayout
      filters={filters}
      table={
        <TopicTable
          topics={topics?.entries ?? []}
          ariaLabel={`Topics overview, page ${topics?.currentPage ?? 0} of ${
            topics?.totalPages ?? 0
          }`}
        />
      }
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}
