import {
  Alert,
  ChipStatus,
  DataTable,
  DataTableColumn,
  Flexbox,
  GhostButton,
  Icon,
  StatusChip,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import DetailsModalContent from "src/app/features/approvals/acls/components/DetailsModalContent";
import useTableFilters from "src/app/features/approvals/acls/hooks/useTableFilters";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequest, AclRequestsForApprover } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";

interface AclRequestTableRow {
  id: number;
  acl_ssl: string[];
  acl_ip: string[];
  topicname: AclRequest["topicname"];
  prefixed: boolean;
  environmentName: string;
  teamname: AclRequest["teamname"];
  aclType: AclRequest["aclType"];
  username: string;
  requesttimestring: string;
  requestStatus: "CREATED" | "DELETED" | "DECLINED" | "APPROVED" | "ALL" | "-";
}

const getRows = (entries: AclRequest[] | undefined): AclRequestTableRow[] => {
  if (entries === undefined) {
    return [];
  }
  return entries.map(
    ({
      req_no,
      acl_ssl,
      acl_ip,
      topicname,
      aclPatternType,
      environmentName,
      teamname,
      aclType,
      username,
      requesttimestring,
      requestStatus,
    }) => ({
      id: Number(req_no),
      acl_ssl: acl_ssl ?? [],
      acl_ip: acl_ip ?? [],
      topicname: topicname,
      prefixed: aclPatternType === "PREFIXED",
      environmentName: environmentName ?? "-",
      teamname,
      aclType,
      username: username ?? "-",
      requesttimestring: requesttimestring ?? "-",
      requestStatus: requestStatus ?? "-",
    })
  );
};

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));
  const [activePage, setActivePage] = useState(initialPage || 1);

  const [detailsModal, setDetailsModal] = useState({
    isOpen: false,
    reqNo: "",
  });
  const [declineModal, setDeclineModal] = useState({
    isOpen: false,
    reqNo: "",
  });

  const [errorMessage, setErrorMessage] = useState("");

  const { environment, status, aclType, topic, filters } = useTableFilters();

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const { data, isLoading, isError, error } = useQuery<
    AclRequestsForApprover,
    Error
  >({
    queryKey: ["aclRequests", activePage, environment, status, aclType, topic],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(activePage),
        env: environment,
        requestStatus: status,
        aclType,
        topic,
      }),
    onSuccess: (data) => {
      // If through filtering a user finds themselves on a non existent page, reset page to 1
      // For example:
      // - one request returns 4 pages of results
      // - navigate to page 4
      // - change filters, to a request that returns 1 page of results
      // - if not redirected to page 1, table won't be able to handle pagination (clicking "Back" will set page at -1)
      if (data.entries.length === 0 && activePage !== 1) {
        handleChangePage(1);
      }
    },
    keepPreviousData: true,
  });

  const { isLoading: approveIsLoading, mutate: approveRequest } = useMutation({
    mutationFn: approveAclRequest,
    onSuccess: (responses) => {
      const response = responses[0];
      if (response.result !== "success") {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDetailsModal({ isOpen: false, reqNo: "" });

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        return handleChangePage(activePage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const { isLoading: declineIsLoading, mutate: declineRequest } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: (responses) => {
      const response = responses[0];
      if (response.result !== "success") {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDeclineModal({ isOpen: false, reqNo: "" });

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        return handleChangePage(activePage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const columns: Array<DataTableColumn<AclRequestTableRow>> = [
    {
      type: "custom",
      field: "acl_ssl",
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ acl_ssl }: AclRequestTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {acl_ssl.map((ssl, index) => (
              <StatusChip
                status="neutral"
                key={`${ssl}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${ssl} `}
              />
            ))}
          </Flexbox>
        );
      },
    },
    {
      type: "custom",
      field: "acl_ip",
      headerName: "IP addresses",
      UNSAFE_render: ({ acl_ip }: AclRequestTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {acl_ip.map((ip, index) => (
              <StatusChip
                status="neutral"
                key={`${ip}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${ip} `}
              />
            ))}
          </Flexbox>
        );
      },
    },
    {
      type: "custom",
      field: "topicname",
      headerName: "Topic",
      UNSAFE_render({ topicname, prefixed }: AclRequestTableRow) {
        return (
          <>
            {topicname}
            {prefixed && <code>(prefixed)</code>}
          </>
        );
      },
    },
    {
      type: "status",
      field: "environmentName",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
    {
      type: "text",
      field: "teamname",
      headerName: "Team",
    },
    {
      type: "status",
      field: "aclType",
      headerName: "ACL type",
      status: ({ aclType }) => ({
        status: aclType === "CONSUMER" ? "success" : "info",
        text: aclType,
      }),
    },
    {
      type: "status",
      field: "requestStatus",
      headerName: "Status",
      status: ({ requestStatus }) => {
        const statusKind: {
          [key in AclRequestTableRow["requestStatus"]]: ChipStatus;
        } = {
          CREATED: "info",
          DELETED: "danger",
          DECLINED: "warning",
          APPROVED: "success",
          ALL: "neutral",
          "-": "neutral",
        };
        return {
          status: statusKind[requestStatus],
          text: requestStatus,
        };
      },
    },
    {
      type: "text",
      field: "username",
      headerName: "Requested by",
    },
    {
      type: "text",
      field: "requesttimestring",
      headerName: "Requested on",
      formatter: (value) => {
        return `${value} UTC`;
      },
    },
    {
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id }: AclRequestTableRow) => {
        return (
          <GhostButton
            icon={infoSign}
            onClick={() => setDetailsModal({ isOpen: true, reqNo: String(id) })}
            title={"View request details"}
            dense
          >
            View details
          </GhostButton>
        );
      },
    },
    {
      width: 30,
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id, requestStatus }: AclRequestTableRow) => {
        const [isLoading, setIsLoading] = useState(false);
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => {
                setIsLoading(true);
                return approveRequest({
                  requestEntityType: "ACL",
                  reqIds: [String(id)],
                });
              }}
              title={"Approve request"}
            >
              {isLoading && approveIsLoading ? (
                <Icon color="grey-70" icon={loadingIcon} />
              ) : (
                <Icon color="grey-70" icon={tickCircle} />
              )}
            </GhostButton>
          );
        }
      },
    },
    {
      width: 30,
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id, requestStatus }: AclRequestTableRow) => {
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() =>
                setDeclineModal({ isOpen: true, reqNo: String(id) })
              }
              title={"Decline request"}
              disabled={approveIsLoading}
            >
              <Icon color="grey-70" icon={deleteIcon} />
            </GhostButton>
          );
        }
      },
    },
  ];

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const selectedRequest = data?.entries.find(
    (request) => request.req_no === Number(detailsModal.reqNo)
  );

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: "" })}
          onApprove={() => {
            approveRequest({
              requestEntityType: "ACL",
              reqIds: [detailsModal.reqNo],
            });
          }}
          onDecline={() => {
            setDetailsModal({ isOpen: false, reqNo: "" });
            setDeclineModal({ isOpen: true, reqNo: detailsModal.reqNo });
          }}
          isLoading={approveIsLoading}
          disabledActions={selectedRequest?.requestStatus !== "CREATED"}
        >
          <DetailsModalContent aclRequest={selectedRequest} />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={() => setDeclineModal({ isOpen: false, reqNo: "" })}
          onCancel={() => setDeclineModal({ isOpen: false, reqNo: "" })}
          onSubmit={(message: string) => {
            declineRequest({
              requestEntityType: "ACL",
              reqIds: [declineModal.reqNo],
              reason: message,
            });
          }}
          isLoading={declineIsLoading}
        />
      )}
      {errorMessage !== "" && (
        <div role="alert">
          <Alert type="warning">{errorMessage}</Alert>
        </div>
      )}

      <ApprovalsLayout
        filters={filters}
        table={
          <DataTable
            ariaLabel={"Acl requests"}
            columns={columns}
            rows={getRows(data?.entries)}
            noWrap={false}
          />
        }
        pagination={pagination}
        isLoading={isLoading}
        isErrorLoading={isError}
        errorMessage={error}
      />
    </>
  );
}

export default AclApprovals;
