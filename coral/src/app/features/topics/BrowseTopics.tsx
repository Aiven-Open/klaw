import { TopicList } from "src/app/features/topics/list";
import { Pagination } from "src/app/components/Pagination";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";
import { useGetTopics } from "src/app/features/topics/list/hooks/useGetTopics";
import { useState } from "react";
import { SelectEnv } from "src/app/features/topics/select-env";
import { useGetEnvs } from "src/app/features/topics/select-env/hooks/useGetEnvs";
import { TopicEnv } from "src/domain/topics";
import { SelectTeam } from "src/app/features/topics/select-team";
import { useGetTeams } from "src/app/features/topics/select-team/hooks/useGetTeams";

function BrowseTopics() {
  const [page, setPage] = useState(1);
  const [env, setEnv] = useState<TopicEnv>(TopicEnv.ALL);
  const [team, setTeam] = useState<string>("All teams");

  const { data: topicEnvs } = useGetEnvs();
  const { data: topicTeams } = useGetTeams();

  const {
    data: topics,
    isLoading,
    isError,
    isPreviousData,
  } = useGetTopics({ currentPage: page, topicEnv: env, teamName: team });

  const hasTopics = topics && topics.entries.length > 0;
  const hasMultiplePages = topics && topics.totalPages > 1;

  return (
    <>
      <Flexbox direction={"row"} colGap={"l4"}>
        {topicEnvs && (
          <FlexboxItem width={"l7"}>
            <SelectEnv
              envOptions={[...topicEnvs, TopicEnv.ALL]}
              activeOption={env}
              selectEnv={setEnv}
            />
          </FlexboxItem>
        )}
        {topicTeams && (
          <FlexboxItem width={"l7"}>
            <SelectTeam
              teamOptions={[...topicTeams, "All teams"]}
              activeOption={team}
              selectTeam={setTeam}
            />
          </FlexboxItem>
        )}
      </Flexbox>

      <Flexbox direction={"column"} alignItems={"center"} rowGap={"l4"}>
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
    </>
  );
}

export default BrowseTopics;
