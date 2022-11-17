import { TopicList } from "src/app/features/topics/list";
import { Pagination } from "src/app/components/Pagination";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";
import { useGetTopics } from "src/app/features/topics/list/hooks/useGetTopics";
import { useState } from "react";
import { SelectEnv } from "src/app/features/topics/select-env";

type Env = "ALL" | "DEV" | "TST";
const mockedEnvOptions: Env[] = ["ALL", "DEV", "TST"];

function BrowseTopics() {
  const [page, setPage] = useState(1);
  const [env, setEnv] = useState<Env>("ALL");
  const { data, isLoading, isError } = useGetTopics(page);
  const hasTopics = data && data.entries.length > 0;
  const hasMultiplePages = data && data.totalPages > 1;

  return (
    <Flexbox direction={"column"} alignItems={"center"} rowGap={"l4"}>
      <FlexboxItem alignSelf={"self-start"}>
        <SelectEnv
          envOptions={mockedEnvOptions}
          activeOption={env}
          selectEnv={setEnv}
        />
      </FlexboxItem>
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
