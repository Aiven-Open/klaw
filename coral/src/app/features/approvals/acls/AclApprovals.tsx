import {
  Alert,
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
import RequestRejectModal from "src/app/features/approvals/components/RequestRejectModal";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequest, AclRequestsForApprover } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";

interface AclRequestTableRows {
  id: number;
  acl_ssl: string[];
  acl_ip: string[];
  topicname: AclRequest["topicname"];
  prefixed: boolean;
  environmentName: string;
  teamname: AclRequest["teamname"];
  topictype: AclRequest["topictype"];
  username: string;
  requesttimestring: string;
}

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));
  const [activePage, setActivePage] = useState(initialPage || 1);

  const [detailsModal, setDetailsModal] = useState({
    isOpen: false,
    reqNo: "",
  });
  const [rejectModal, setRejectModal] = useState({ isOpen: false, reqNo: "" });

  const [errorMessage, setErrorMessage] = useState("");

  const { environment, status, aclType, topic, filters } = useTableFilters();

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const { data, isLoading } = useQuery<AclRequestsForApprover, Error>({
    queryKey: ["aclRequests", activePage, environment, status, aclType, topic],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(activePage),
        env: environment,
        requestsType: status,
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
      console.log(data);
      if (data.entries.length === 0) {
        handleChangePage(1);
      }
    },
    keepPreviousData: true,
  });

  const { isLoading: approveIsLoading, mutate: approveRequest } = useMutation({
    mutationFn: approveAclRequest,
    onSuccess: (response) => {
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
        return setActivePage(activePage - 1);
      }

      // We need to invalidate the query populating the table to reflect the change
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const { isLoading: rejectIsLoading, mutate: rejectRequest } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: (response) => {
      if (response.result !== "success") {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setRejectModal({ isOpen: false, reqNo: "" });

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        return setActivePage(activePage - 1);
      }

      // We need to invalidate the query populating the table to reflect the change
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  if (data === undefined || isLoading) {
    return <SkeletonTable />;
  }

  const columns: Array<DataTableColumn<AclRequestTableRows>> = [
    {
      type: "custom",
      field: "acl_ssl",
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ acl_ssl }: AclRequestTableRows) => {
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
      UNSAFE_render: ({ acl_ip }: AclRequestTableRows) => {
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
      UNSAFE_render({ topicname, prefixed }: AclRequestTableRows) {
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
      field: "topictype",
      headerName: "ACL type",
      status: ({ topictype }) => ({
        status: topictype === "Consumer" ? "success" : "info",
        text: topictype,
      }),
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
      UNSAFE_render: ({ id }: AclRequestTableRows) => {
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
      UNSAFE_render: ({ id }: AclRequestTableRows) => {
        const [isLoading, setIsLoading] = useState(false);

        return (
          <GhostButton
            onClick={() => {
              setIsLoading(true);
              return approveRequest({ req_no: String(id) });
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
      },
    },
    {
      width: 30,
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id }: AclRequestTableRows) => {
        return (
          <GhostButton
            onClick={() => setRejectModal({ isOpen: true, reqNo: String(id) })}
            title={"Reject request"}
          >
            <Icon color="grey-70" icon={deleteIcon} />
          </GhostButton>
        );
      },
    },
  ];

  const rows: AclRequestTableRows[] = data.entries.map(
    ({
      req_no,
      acl_ssl,
      acl_ip,
      topicname,
      aclPatternType,
      environmentName,
      teamname,
      topictype,
      username,
      requesttimestring,
    }) => ({
      id: Number(req_no),
      acl_ssl: acl_ssl ?? [],
      acl_ip: acl_ip ?? [],
      topicname: topicname,
      prefixed: aclPatternType === "PREFIXED",
      environmentName: environmentName ?? "-",
      teamname,
      topictype,
      username: username ?? "-",
      requesttimestring: requesttimestring ?? "-",
    })
  );

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: "" })}
          onApprove={() => {
            approveRequest({ req_no: detailsModal.reqNo });
          }}
          onReject={() => {
            setDetailsModal({ isOpen: false, reqNo: "" });
            setRejectModal({ isOpen: true, reqNo: detailsModal.reqNo });
          }}
          isLoading={approveIsLoading}
        >
          <DetailsModalContent
            aclRequest={data.entries.find(
              (request) => request.req_no === Number(detailsModal.reqNo)
            )}
          />
        </RequestDetailsModal>
      )}
      {rejectModal.isOpen && (
        <RequestRejectModal
          onClose={() => setRejectModal({ isOpen: false, reqNo: "" })}
          onCancel={() => setRejectModal({ isOpen: false, reqNo: "" })}
          onSubmit={(message: string) => {
            rejectRequest({
              req_no: rejectModal.reqNo,
              reasonForDecline: message,
            });
          }}
          isLoading={rejectIsLoading}
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
            rows={rows}
            noWrap={false}
          />
        }
        pagination={
          <Pagination
            activePage={data.currentPage}
            totalPages={data.totalPages}
            setActivePage={handleChangePage}
          />
        }
      />
    </>
  );
}

export default AclApprovals;
