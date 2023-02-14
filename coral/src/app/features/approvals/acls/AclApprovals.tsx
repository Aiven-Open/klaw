import { DataTableColumn, Flexbox, StatusChip } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import RequestApprovalTable from "src/app/features/approvals/components/RequestApprovalTable";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequest, AclRequestsForApprover } from "src/domain/acl/acl-types";

interface AclRequestTableData {
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
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));
  const [activePage, setActivePage] = useState(initialPage || 1);

  const { data, isLoading } = useQuery<AclRequestsForApprover, Error>({
    queryKey: ["aclRequests", activePage],
    queryFn: () => getAclRequestsForApprover({ pageNo: String(activePage) }),
    keepPreviousData: true,
  });

  const columns: Array<DataTableColumn<AclRequestTableData>> = [
    {
      type: "custom",
      field: "acl_ssl",
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ acl_ssl }: AclRequestTableData) => {
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
      UNSAFE_render: ({ acl_ip }: AclRequestTableData) => {
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
      UNSAFE_render({ topicname, prefixed }: AclRequestTableData) {
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
      headerName: "Cluster",
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
      headerName: "Date requested",
    },
  ];

  const getRows = (): AclRequestTableData[] => {
    if (data === undefined) {
      return [];
    }
    return data.entries.map(
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
  };

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  return (
    <>
      {data === undefined || isLoading ? (
        <SkeletonTable />
      ) : (
        <RequestApprovalTable<AclRequestTableData>
          name={"Acl requests"}
          columns={columns}
          rows={getRows()}
          dataQueryKey={["aclRequests", activePage]}
          approveFn={approveAclRequest}
          rejectFn={declineAclRequest}
        />
      )}
      <Pagination
        activePage={data?.currentPage || 1}
        totalPages={data?.totalPages || 1}
        setActivePage={handleChangePage}
      />
    </>
  );
}

export default AclApprovals;
