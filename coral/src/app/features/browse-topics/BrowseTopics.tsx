import { useGetTopics } from "src/app/features/browse-topics/hooks/topic-list/useGetTopics";
import { Pagination } from "src/app/components/Pagination";
import SelectTeam from "src/app/features/browse-topics/components/select-team/SelectTeam";
import TopicList from "src/app/features/browse-topics/components/topic-list/TopicList";
import { useGetTeams } from "src/app/features/browse-topics/hooks/teams/useGetTeams";
import { useState } from "react";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";
import { useSearchParams } from "react-router-dom";
import { Environment } from "src/domain/environment";
import SelectEnvironment from "src/app/features/browse-topics/components/select-environment/SelectEnvironment";
import { useGetEnvironments } from "src/app/features/browse-topics/hooks/environment/useGetEnvironments";

// Use a UUID value to represent empty option value.
const ALL_TEAMS_VALUE = "f5ed03b4-c0da-4b18-a534-c7e9a13d1342";
const ALL_ENVIRONMENTS_VALUE = "ALL";

function BrowseTopics() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [page, setPage] = useState(1);
  const [environment, setEnvironment] = useState<Environment>(
    ALL_ENVIRONMENTS_VALUE
  );
  const [team, setTeam] = useState<string>(ALL_TEAMS_VALUE);

  const { data: topicEnvs } = useGetEnvironments();
  const { data: topicTeams } = useGetTeams();

  const {
    data: topics,
    isLoading,
    isError,
    isPreviousData,
  } = useGetTopics({
    currentPage: page,
    environment,
    ...(team !== ALL_TEAMS_VALUE && { teamName: team }),
  });

  const hasTopics = topics && topics.entries.length > 0;
  const hasMultiplePages = topics && topics.totalPages > 1;

  return (
    <>
      <Flexbox direction={"row"} colGap={"l4"}>
        {topicEnvs && (
          <FlexboxItem width={"l7"}>
            <SelectEnvironment
              environments={[
                { label: "All Environments", value: ALL_ENVIRONMENTS_VALUE },
                ...topicEnvs.map((env) => ({ label: env, value: env })),
              ]}
              activeOption={environment}
              selectEnvironment={selectEnvironment}
            />
          </FlexboxItem>
        )}
        {topicTeams && (
          <FlexboxItem width={"l7"}>
            <SelectTeam
              teams={[
                { label: "All teams", value: ALL_TEAMS_VALUE },
                ...topicTeams.map((team) => ({ label: team, value: team })),
              ]}
              activeOption={team}
              selectTeam={selectTeam}
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

  function selectEnvironment(environment: Environment) {
    setEnvironment(environment);
    if (environment === ALL_ENVIRONMENTS_VALUE) {
      searchParams.delete("environment");
    } else {
      searchParams.set("environment", environment);
    }
    setSearchParams(searchParams);
  }

  function selectTeam(team: string) {
    setTeam(team);
    if (team === ALL_TEAMS_VALUE) {
      searchParams.delete("team");
    } else {
      searchParams.set("team", team);
    }
    setSearchParams(searchParams);
  }
}

export default BrowseTopics;
