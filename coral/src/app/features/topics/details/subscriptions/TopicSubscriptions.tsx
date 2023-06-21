import {
  Alert,
  BorderBox,
  Box,
  Grid,
  PageHeader,
  SegmentedControl,
  SegmentedControlGroup,
  useToast,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useMutation } from "@tanstack/react-query";
import pick from "lodash/pick";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Dialog } from "src/app/components/Dialog";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import {
  FiltersProvider,
  useFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";
import { TopicSubscriptionsTable } from "src/app/features/topics/details/subscriptions/TopicSubscriptionsTable";
import TopicSubscriptionsDetailsModal from "src/app/features/topics/details/subscriptions/components/TopicSubscriptionsDetailsModal";
import { getTopicStats } from "src/app/features/topics/details/utils";
import { createAclDeletionRequest } from "src/domain/acl/acl-api";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { parseErrorMsg } from "src/services/mutation-utils";

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
  const navigate = useNavigate();
  const {
    topicOverview,
    environmentId,
    topicName,
    offsetsData,
    serviceAccountData,
    setConsumerGroupId,
    setAclReqNo,
  } = useTopicDetails();

  const [deleteModal, setDeleteModal] = useState<{
    isOpen: boolean;
    req_no: string | null;
  }>({ isOpen: false, req_no: null });

  const [detailsModal, setDetailsModal] = useState<{
    isOpen: boolean;
    isAivenCluster: boolean;
    aclReqNo?: string;
  }>({ isOpen: false, isAivenCluster: false, aclReqNo: undefined });

  const toast = useToast();

  const { teamId, aclType, search } = useFiltersContext();

  const { isLoading: deleteIsLoading, mutate: deleteRequest } = useMutation({
    mutationFn: createAclDeletionRequest,
    onSuccess: () => {
      setErrorMessage("");
      setDeleteModal({ isOpen: false, req_no: null });
      toast({
        message: "Subscription deletion request successfully created",
        position: "bottom-left",
        variant: "default",
      });
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const [errorMessage, setErrorMessage] = useState("");

  const [selectedSubs, setSelectedSubs] =
    useState<SubscriptionOptions>("aclInfoList");

  const openDeleteModal = (req_no: string) => {
    setDeleteModal({ isOpen: true, req_no });
  };

  const closeDeleteModal = () => {
    setDeleteModal({ isOpen: false, req_no: null });
  };

  const handleDelete = (req_no: string | null) => {
    if (req_no === null) {
      throw Error("Cannot delete request with req_no null");
    }
    deleteRequest({ requestId: req_no });
  };

  const openDetailsModal = ({
    isAivenCluster,
    aclReqNo,
    consumerGroupId,
  }: {
    isAivenCluster: boolean;
    aclReqNo: string;
    consumerGroupId?: string;
  }) => {
    setConsumerGroupId(consumerGroupId);
    setAclReqNo(aclReqNo);
    setDetailsModal({ isOpen: true, isAivenCluster, aclReqNo });
  };

  const closeDetailsModal = () => {
    setConsumerGroupId(undefined);
    setAclReqNo(undefined);
    setDetailsModal({
      isOpen: false,
      isAivenCluster: false,
      aclReqNo: undefined,
    });
  };

  const subsStats = useMemo(() => {
    return getTopicStats(topicOverview);
  }, [topicOverview]);

  const filteredData: AclOverviewInfo[] = useMemo(() => {
    const subs = topicOverview[selectedSubs];

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
  }, [search, teamId, aclType, selectedSubs, topicOverview]);

  const selectedSub = filteredData.find(
    ({ req_no }) => req_no === detailsModal.aclReqNo
  );

  return (
    <>
      {deleteModal.isOpen && deleteModal.req_no !== null && (
        <Dialog
          title={"Deletion request"}
          primaryAction={{
            text: "Create deletion request",
            onClick: () => handleDelete(deleteModal.req_no),
            loading: deleteIsLoading,
          }}
          secondaryAction={{
            text: "Cancel",
            onClick: closeDeleteModal,
            disabled: deleteIsLoading,
          }}
          type={"danger"}
        >
          Are you sure you want to delete this subscription? This action will
          create a deletion request for approval.
        </Dialog>
      )}
      {detailsModal.isOpen && selectedSub !== undefined && (
        <TopicSubscriptionsDetailsModal
          closeDetailsModal={closeDetailsModal}
          isAivenCluster={detailsModal.isAivenCluster}
          selectedSub={selectedSub}
          offsetsData={offsetsData}
          serviceAccountData={serviceAccountData}
        />
      )}
      <PageHeader
        title="Subscriptions"
        primaryAction={{
          icon: add,
          text: "Request a subscription",
          onClick: () =>
            navigate(`/topic/${topicName}/subscribe?env=${environmentId}`),
        }}
      />
      {errorMessage !== "" && (
        <Box role="alert" marginBottom={"l2"}>
          <Alert type="error">{errorMessage}</Alert>
        </Box>
      )}

      <Grid cols={"2"} gap={"l2"} marginBottom={"l2"}>
        <BorderBox
          borderColor="grey-5"
          padding={"l2"}
          title="Amount of producer subscriptions"
        >
          <StatsDisplay amount={subsStats.producers} entity={"Producers"} />
        </BorderBox>
        <BorderBox
          borderColor="grey-5"
          padding={"l2"}
          title="Amount of consumer subscriptions"
        >
          <StatsDisplay amount={subsStats.consumers} entity={"Consumers"} />
        </BorderBox>
      </Grid>

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
            onDelete={openDeleteModal}
            onDetails={openDetailsModal}
          />
        }
      />
    </>
  );
};

// Because we need to set the default value of team filter as the user's current team
// We need to use the FiltersProvider directly instead of withFiltersContext
const WithCurrentTeamDefaultFilter = () => {
  const authUser = useAuthContext();

  return (
    <FiltersProvider
      defaultValues={{ paginated: false, teamId: authUser?.teamId }}
    >
      <TopicSubscriptions />
    </FiltersProvider>
  );
};

export default WithCurrentTeamDefaultFilter;
