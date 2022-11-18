import { useGetTopics } from "src/app/features/topics/hooks/list/useGetTopics";
import { Pagination } from "src/app/components/Pagination";
import { TopicEnv } from "src/domain/topics";
import SelectTeam from "src/app/features/topics/components/select-team/SelectTeam";
import { useGetEnvs } from "src/app/features/topics/hooks/env/useGetEnvs";
import TopicList from "src/app/features/topics/components/list/TopicList";
import { useGetTeams } from "src/app/features/topics/hooks/teams/useGetTeams";
import SelectEnv from "src/app/features/topics/components/select-env/SelectEnv";
import { useState } from "react";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";

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
