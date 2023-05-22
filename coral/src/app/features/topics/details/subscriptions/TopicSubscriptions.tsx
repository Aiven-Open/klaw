import { RadioButton, RadioButtonGroup } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import pick from "lodash/pick";
import { useMemo, useState } from "react";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicSubscriptionsTable } from "src/app/features/topics/details/subscriptions/TopicSubscriptionsTable";
import { getTopicOverview } from "src/domain/topic/topic-api";
import { AclOverviewInfo } from "src/domain/topic/topic-types";

type SubscriptionOptions =
  | "aclInfoList"
  | "prefixedAclInfoList"
  | "transactionalAclInfoList";
const isSubscriptionsOption = (value: string): value is SubscriptionOptions => {
  return [
    "aclInfoList",
    "prefixedAclInfoList",
    "transactionalAclInfoList",
  ].includes(value);
};

const TopicSubscriptions = () => {
  // @ TODO get environment from useTopicDetails too when it is implemented
  const { topicName } = useTopicDetails();
  const { search, teamId, aclType } = useFiltersValues();
  const {
    data,
    isLoading: dataIsLoading,
    isError,
    error,
  } = useQuery(["topic-overview"], {
    queryFn: () => getTopicOverview({ topicName, environmentId: "2" }),
  });

  const [selectedSubs, setSelectedSubs] =
    useState<SubscriptionOptions>("aclInfoList");

  const filteredData: AclOverviewInfo[] = useMemo(() => {
    if (data === undefined) {
      return [];
    }

    const subs = data[selectedSubs];

    if (subs === undefined) {
      return [];
    }

    return subs.filter((sub) => {
      const currentTeamId = String(sub.teamid);
      const teamFilter = teamId === "ALL" || currentTeamId === teamId;
      const searchFilter =
        search === "" ||
        // @TODO: add date requested when available in data
        JSON.stringify(pick(sub, "acl_ssl", "acl_ip", "topictype", "teamname"))
          .toLowerCase()
          .includes(search.toLowerCase());
      const aclTypeFilter =
        aclType === "ALL" || sub.topictype?.toUpperCase() === aclType;
      return teamFilter && aclTypeFilter && searchFilter;
    });
  }, [search, teamId, aclType, selectedSubs, data]);

  return (
    <TableLayout
      filters={[
        <TeamFilter key="team" />,
        <AclTypeFilter key="aclType" />,
        <SearchFilter
          key="search"
          placeholder="Search"
          description={`Search for a partial match on any data point. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
        />,
        <RadioButtonGroup
          name="Subscription options"
          key="subscription-options"
          onChange={(value: string) => {
            if (isSubscriptionsOption(value)) {
              setSelectedSubs(value);
            }
          }}
          value={selectedSubs}
        >
          <RadioButton name="User subscriptions" value="aclInfoList">
            User subs
          </RadioButton>
          <RadioButton
            name="Prefixed subscriptions"
            value="prefixedAclInfoList"
          >
            Prefixed subs
          </RadioButton>
          <RadioButton
            name="Transactional subscriptions"
            value="transactionalAclInfoList"
          >
            Transactional subs
          </RadioButton>
        </RadioButtonGroup>,
      ]}
      table={
        <TopicSubscriptionsTable
          selectedSubs={selectedSubs}
          filteredData={filteredData}
        />
      }
      isLoading={dataIsLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
};

export default TopicSubscriptions;
