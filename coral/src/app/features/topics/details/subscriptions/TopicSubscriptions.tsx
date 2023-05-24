import {
  Alert,
  SegmentedControl,
  SegmentedControlGroup,
  useToast,
} from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Dialog } from "src/app/components/Dialog";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicSubscriptionsTable } from "src/app/features/topics/details/subscriptions/TopicSubscriptionsTable";
import { createAclDeletionRequest } from "src/domain/acl/acl-api";
import { getTopicOverview } from "src/domain/topic/topic-api";
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
  // @ TODO get environment from useTopicDetails too when it is implemented
  const { topicName } = useTopicDetails();

  const [deleteModal, setDeleteModal] = useState<{
    isOpen: boolean;
    req_no: string | null;
  }>({ isOpen: false, req_no: null });

  const toast = useToast();

  const {
    data,
    isLoading: dataIsLoading,
    isRefetching: dataIsRefetching,
    isError,
    error,
  } = useQuery(["topic-overview"], {
    queryFn: () => getTopicOverview({ topicName, environmentId: "1" }),
  });

  const { isLoading: deleteIsLoading, mutate: deleteRequest } = useMutation({
    mutationFn: createAclDeletionRequest,
    onSuccess: () => {
      setErrorMessage("");
      setDeleteModal({ isOpen: false, req_no: null });
      toast({
        message: "Subscription deletion request successfully created.",
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
    deleteRequest({ req_no });
  };

  const filteredData: AclOverviewInfo[] = useMemo(() => {
    if (data === undefined) {
      return [];
    }

    const subs = data[selectedSubs];

    if (subs === undefined) {
      return [];
    }

    return subs;
  }, [selectedSubs, data]);

  return (
    <>
      {deleteModal.isOpen && deleteModal.req_no !== null && (
        <Dialog
          title={"Deletion request"}
          primaryAction={{
            text: "Create deletion request",
            onClick: () => handleDelete(deleteModal.req_no),
            loading: deleteIsLoading || dataIsRefetching,
          }}
          secondaryAction={{
            text: "Cancel",
            onClick: closeDeleteModal,
            disabled: deleteIsLoading || dataIsRefetching,
          }}
          type={"danger"}
        >
          Are you sure you want to create a deletion request?
        </Dialog>
      )}
      {errorMessage !== "" && (
        <div role="alert">
          <Alert type="error">{errorMessage}</Alert>
        </div>
      )}

      <TableLayout
        filters={[
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
