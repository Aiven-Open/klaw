import {
  DataTable,
  DataTableColumn,
  Flexbox,
  GhostButton,
  Grid,
  GridItem,
  Icon,
  NativeSelect,
  SearchInput,
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

  const { data, isLoading } = useQuery<AclRequestsForApprover, Error>({
    queryKey: ["aclRequests", activePage],
    queryFn: () => getAclRequestsForApprover({ pageNo: String(activePage) }),
    keepPreviousData: true,
  });

  const { isLoading: approveIsLoading, mutate: approveRequest } = useMutation({
    mutationFn: approveAclRequest,
    onSuccess: (data) => {
      if (data.result !== "success") {
        return alert(data.result);
      }
      setDetailsModal({ isOpen: false, reqNo: "" });
      // We need to invalidate the query populating the table to reflect the change
      queryClient.invalidateQueries(["aclRequests", activePage]);
    },
  });

  const { isLoading: rejectIsLoading, mutate: rejectRequest } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: (data) => {
      if (data.result !== "success") {
        return alert(data.result);
      }
      setRejectModal({ isOpen: false, reqNo: "" });
      // We need to invalidate the query populating the table to reflect the change
      queryClient.invalidateQueries(["aclRequests", activePage]);
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

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const filters = [
    <NativeSelect labelText={"Filter by Topic"} key={"filter-topic"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <NativeSelect
      labelText={"Filter by Environment"}
      key={"filter-environment"}
    >
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <NativeSelect labelText={"Filter by status"} key={"filter-status"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <NativeSelect labelText={"Filter by ACL type"} key={"filter-acl-type"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={"Search for..."}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Press &quot;Enter&quot; to start your search. Press &quot;Escape&quot;
        to delete all your input.
      </div>
    </div>,
  ];

  const renderRequestDetails = (request?: AclRequest) => {
    if (request === undefined) {
      return <div>Request not found.</div>;
    }

    const {
      topictype,
      environmentName = "Environment not found",
      topicname,
      acl_ssl,
      acl_ip,
      consumergroup = "Consumer group not found",
      remarks,
      requesttimestring = "Request time not found",
      username = "User not found",
      requestingTeamName = "Team name not found",
    } = request;

    const ips = acl_ip || [];
    const principals = acl_ssl || [];

    const Label = ({ children }: { children: React.ReactNode }) => (
      <label className="inline-block mb-2 typography-small-strong text-grey-60">
        {children}
      </label>
    );

    return (
      <Grid cols={"2"} rows={"6"} rowGap={"6"}>
        <GridItem>
          <Flexbox direction={"column"} width={"min"}>
            <Label>ACL type</Label>
            <div>
              <StatusChip
                status={topictype === "Producer" ? "info" : "success"}
                text={topictype}
              />
            </div>
          </Flexbox>
        </GridItem>
        <GridItem>
          <Flexbox direction={"column"}>
            <Label>Requesting team</Label>
            {requestingTeamName}
          </Flexbox>
        </GridItem>
        <GridItem>
          <Flexbox direction={"column"} width={"min"}>
            <Label>Environment</Label>
            <div>
              <StatusChip status={"neutral"} text={environmentName} />
            </div>
          </Flexbox>
        </GridItem>
        <GridItem>
          <Flexbox direction={"column"}>
            <Label>Topic</Label>
            {topicname}
          </Flexbox>
        </GridItem>
        <GridItem colSpan={"span-2"}>
          <Flexbox direction={"column"}>
            <Label>Principals/Usernames</Label>
            <Flexbox direction={"row"} gap={"2"}>
              {principals.map((principal) => (
                <StatusChip
                  key={principal}
                  status={"neutral"}
                  text={`${principal} `}
                />
              ))}
            </Flexbox>
          </Flexbox>
        </GridItem>
        {ips.length > 0 && (
          <GridItem colSpan={"span-2"}>
            <Flexbox direction={"column"}>
              <Label>IP addresses</Label>
              <Flexbox direction={"row"} gap={"2"}>
                {ips.map((ip) => (
                  <StatusChip key={ip} status={"neutral"} text={`${ip} `} />
                ))}
              </Flexbox>
            </Flexbox>
          </GridItem>
        )}
        <GridItem colSpan={"span-2"}>
          <Flexbox direction={"column"}>
            <Label>Consumer group</Label>
            {topictype === "Consumer" ? consumergroup : <i>Not applicable</i>}
          </Flexbox>
        </GridItem>
        <GridItem colSpan={"span-2"}>
          <Flexbox direction={"column"}>
            <Label>Message for the approver</Label>
            {remarks || <i>No message</i>}
          </Flexbox>
        </GridItem>
        <GridItem>
          <Flexbox direction={"column"}>
            <Label> Request by</Label>
            {username}
          </Flexbox>
        </GridItem>
        <GridItem>
          <Flexbox direction={"column"}>
            <Label> Requested on</Label>
            {requesttimestring} UTC
          </Flexbox>
        </GridItem>
      </Grid>
    );
  };

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
          {renderRequestDetails(
            data.entries.find(
              (request) => request.req_no === Number(detailsModal.reqNo)
            )
          )}
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
