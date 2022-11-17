import { TopicList } from "src/app/features/topics/list";
import { Pagination } from "src/app/components/Pagination";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";
import { useGetTopics } from "src/app/features/topics/list/hooks/useGetTopics";
import { useState } from "react";
import { SelectEnv } from "src/app/features/topics/select-env";
import { useGetEnvs } from "src/app/features/topics/select-env/hooks/useGetEnvs";
import { TopicEnv } from "src/domain/topics";

function BrowseTopics() {
  const [page, setPage] = useState(1);
  const [env, setEnv] = useState<TopicEnv>("ALL");

  const { data: topicEnvs } = useGetEnvs();

  const {
    data: topics,
    isLoading,
    isError,
    isPreviousData,
  } = useGetTopics({ currentPage: page, topicEnv: env });
  const hasTopics = topics && topics.entries.length > 0;
  const hasMultiplePages = topics && topics.totalPages > 1;

  return (
    <Flexbox direction={"column"} alignItems={"center"} rowGap={"l4"}>
      <FlexboxItem alignSelf={"self-start"}>
        {topicEnvs && (
          <SelectEnv
            envOptions={[...topicEnvs, "ALL"]}
            activeOption={env}
            selectEnv={setEnv}
          />
        )}
      </FlexboxItem>
      {isPreviousData && <div>Filtering list...</div>}
      {isLoading && <div>Loading...</div>}
      {isError && <div>Something went wrong 😔</div>}

      {!isLoading && !hasTopics && <div>No topics found</div>}
      {hasTopics && <TopicList topics={topics.entries} />}

      {hasMultiplePages && (
        <Pagination
          activePage={topics.currentPage}
          totalPages={topics.totalPages}
          setActivePage={setPage}
        />
      )}
    </Flexbox>
  );
}

export default BrowseTopics;
