import {
  DataTable,
  DataTableColumn,
  Flexbox,
  GhostButton,
  Icon,
  StatusChip,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import { getAclRequestsForApprover } from "src/domain/acl/acl-api";
import { AclRequest, AclRequestsForApprover } from "src/domain/acl/acl-types";

interface AclRequestTableData {
  id: number;
  acl_ssl: string[];
  acl_ip: string[];
  topicname: AclRequest["topicname"];
  environmentName: string;
  teamname: AclRequest["teamname"];
  topictype: AclRequest["topictype"];
  username: string;
  requesttime: string;
}

const columns: Array<DataTableColumn<AclRequestTableData>> = [
  {
    type: "custom",
    field: "acl_ssl",
    headerName: "Principals/Usernames",
    UNSAFE_render: ({ acl_ssl }: AclRequestTableData) => {
      return (
        <Flexbox wrap={"wrap"} gap={"2"}>
          {acl_ssl.map((ssl, index) => (
            <StatusChip status="neutral" key={`${ssl}-${index}`} text={ssl} />
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
            <StatusChip status="neutral" key={`${ip}-${index}`} text={ip} />
          ))}
        </Flexbox>
      );
    },
  },
  {
    type: "text",
    field: "topicname",
    headerName: "Topic",
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
    field: "requesttime",
    headerName: "Date requested",
    formatter: (value) => {
      const date = new Date(value);

      const dateTimeString = Intl.DateTimeFormat("en", {
        dateStyle: "medium",
        timeStyle: "medium",
        hourCycle: "h24",
      }).format(date);

      const timezoneString = Intl.DateTimeFormat("en", {
        timeZoneName: "short",
      })
        .format(date)
        .split(",")[1];

      return `${dateTimeString} ${timezoneString}`;
    },
  },
  {
    width: 30,
    headerName: "Approve",
    type: "custom",
    UNSAFE_render: () => {
      return (
        <Flexbox justifyContent={"center"}>
          <GhostButton onClick={() => alert("Approve")} title={"Approve"}>
            <Icon color="grey-70" icon={tickCircle} />
          </GhostButton>
        </Flexbox>
      );
    },
  },
  {
    width: 30,
    headerName: "Decline",
    type: "custom",
    UNSAFE_render: () => {
      return (
        <Flexbox justifyContent={"center"}>
          <GhostButton
            onClick={() => alert("Decline")}
            title={"Decline"}
            style={{ textAlign: "center" }}
          >
            <Icon color="grey-70" icon={deleteIcon} />
          </GhostButton>
        </Flexbox>
      );
    },
  },
];

// const placeholderData: AclRequestTableData[] = [
//   {
//     id: 0,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 1,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 2,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 3,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 4,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 5,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 6,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 7,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 8,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
//   {
//     id: 9,
//     acl_ssl: ["..."],
//     acl_ip: ["..."],
//     topicname: "...",
//     environmentName: "...",
//     teamname: "...",
//     topictype: "Consumer",
//     username: "...",
//     requesttime: "2022-12-20T13:01:47.409+00:00",
//   },
// ];

function AclApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));

  const [activePage, setActivePage] = useState(initialPage || 1);

  const { data, isPreviousData } = useQuery<AclRequestsForApprover, Error>({
    queryKey: ["aclRequests", activePage],
    queryFn: () => getAclRequestsForApprover({ pageNo: String(activePage) }),
    keepPreviousData: true,
  });

  if (data === undefined) {
    return <SkeletonTable />;
  }

  const tableData: AclRequestTableData[] = data.entries.map(
    ({
      req_no,
      acl_ssl,
      acl_ip,
      topicname,
      environmentName,
      teamname,
      topictype,
      username,
      requesttime,
    }) => ({
      id: Number(req_no),
      acl_ssl: acl_ssl ?? [],
      acl_ip: acl_ip ?? [],
      topicname: topicname,
      environmentName: environmentName ?? "-",
      teamname,
      topictype,
      username: username ?? "-",
      requesttime: requesttime ?? "-",
    })
  );

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  return (
    <div style={{ opacity: isPreviousData ? "0.7" : "1" }}>
      <DataTable
        ariaLabel={"Acl requests"}
        columns={columns}
        rows={tableData}
        noWrap={false}
      />
      <Pagination
        activePage={data.currentPage}
        totalPages={data.totalPages}
        setActivePage={handleChangePage}
      />
    </div>
  );
}

export default AclApprovals;
