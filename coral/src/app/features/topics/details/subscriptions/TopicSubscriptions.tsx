import {
  PageHeader,
  SegmentedControl,
  SegmentedControlGroup,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useQuery } from "@tanstack/react-query";
import pick from "lodash/pick";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicSubscriptionsTable } from "src/app/features/topics/details/subscriptions/TopicSubscriptionsTable";
import { getTopicOverview } from "src/domain/topic/topic-api";
import { AclOverviewInfo } from "src/domain/topic/topic-types";

const TEMP_ENV_VALUE = "2";

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
  const navigate = useNavigate();
  const { topicName } = useTopicDetails();
  const { search, teamId, aclType } = useFiltersValues();
  const {
    data,
    isLoading: dataIsLoading,
    isError,
    error,
  } = useQuery(["topic-overview"], {
    queryFn: () =>
      getTopicOverview({ topicName, environmentId: TEMP_ENV_VALUE }),
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

    // Early return to avoid running superfluous and potentially expensive Array.filter operations
    if (teamId === "ALL" && search === "" && aclType === "ALL") {
      return subs;
    }

    return subs.filter((sub) => {
      const currentTeamId = String(sub.teamid);
      const teamFilter = teamId === "ALL" || currentTeamId === teamId;
      const searchFilter =
        search === "" ||
        JSON.stringify(pick(sub, "acl_ssl", "acl_ip"))
          .toLowerCase()
          .includes(search.toLowerCase());
      const aclTypeFilter =
        aclType === "ALL" || sub.topictype?.toUpperCase() === aclType;
      return teamFilter && aclTypeFilter && searchFilter;
    });
  }, [search, teamId, aclType, selectedSubs, data]);

  return (
    <>
      <PageHeader
        title="Subscriptions"
        primaryAction={{
          icon: add,
          text: "Request a subscription",
          onClick: () =>
            navigate(`/topic/${topicName}/subscribe?env=${TEMP_ENV_VALUE}`),
        }}
      />

      <TableLayout
        filters={[
          <TeamFilter key="team" />,
          <AclTypeFilter key="aclType" />,
          <SearchFilter
            key="search"
            placeholder="Search principal or IP"
            description={`Search for a partial match principals or IPs. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
          />,
          <SegmentedControlGroup
            name="Subscription options"
            key="subscription-options"
            onChange={(value: string) => {
              if (isSubscriptionsOption(value)) {
                setSelectedSubs(value);
              }
            }}
            value={selectedSubs}
          >
            <SegmentedControl name="User subscriptions" value="aclInfoList">
              User subs.
            </SegmentedControl>
            <SegmentedControl
              name="Prefixed subscriptions"
              value="prefixedAclInfoList"
            >
              Prefixed subs.
            </SegmentedControl>
            <SegmentedControl
              name="Transactional subscriptions"
              value="transactionalAclInfoList"
            >
              Transactional subs.
            </SegmentedControl>
          </SegmentedControlGroup>,
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
    </>
  );
};

export default TopicSubscriptions;
