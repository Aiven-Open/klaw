import { TopicList } from "src/app/features/topics/list";
import { Pagination } from "src/app/components/Pagination";
import { Flexbox } from "@aivenio/design-system";
import { useGetTopics } from "src/app/features/topics/list/hooks/useGetTopics";
import { useState } from "react";

function BrowseTopics() {
  const [page, setPage] = useState(1);
  const { data, isLoading, isError } = useGetTopics(page);
  const hasTopics = data && data.entries.length > 0;
  const hasMultiplePages = data && data.totalPages > 1;

  return (
    <Flexbox direction={"column"} alignItems={"center"} rowGap={"l4"}>
      {isLoading && <div>Loading...</div>}
      {isError && <div>Something went wrong 😔</div>}

      {!hasTopics && <div>No topics found</div>}
      {hasTopics && <TopicList topics={data.entries} />}

      {hasMultiplePages && (
        <Pagination
          activePage={data.currentPage}
          totalPages={data.totalPages}
          setActivePage={setPage}
        />
      )}
    </Flexbox>
  );
}

export default BrowseTopics;
