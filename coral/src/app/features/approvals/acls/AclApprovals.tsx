import {
  DataTable,
  DataTableColumn,
  Flexbox,
  GhostButton,
  Icon,
  NativeSelect,
  SearchInput,
  StatusChip,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import { getAclRequestsForApprover } from "src/domain/acl/acl-api";
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
    headerName: "Date requested",
  },
  {
    // Not having a headerName triggers React error:
    // Warning: Encountered two children with the same key, ``.
    headerName: "",
    type: "custom",
    UNSAFE_render: () => {
      return (
        <GhostButton
          icon={infoSign}
          onClick={() => alert("Details modal with approve and reject buttons")}
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
    UNSAFE_render: () => {
      return (
        <GhostButton
          onClick={() => alert("Approve request right away")}
          title={"Approve request"}
        >
          <Icon color="grey-70" icon={tickCircle} />
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
    UNSAFE_render: () => {
      return (
        <GhostButton
          onClick={() => alert("Reject modal with form for reason")}
          title={"Reject request"}
        >
          <Icon color="grey-70" icon={deleteIcon} />
        </GhostButton>
      );
    },
  },
];

function AclApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));

  const [activePage, setActivePage] = useState(initialPage || 1);

  const { data, isLoading } = useQuery<AclRequestsForApprover, Error>({
    queryKey: ["aclRequests", activePage],
    queryFn: () => getAclRequestsForApprover({ pageNo: String(activePage) }),
    keepPreviousData: true,
  });

  if (data === undefined || isLoading) {
    return <SkeletonTable />;
  }

  const tableData: AclRequestTableData[] = data.entries.map(
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

  return (
    <ApprovalsLayout
      filters={filters}
      table={
        <DataTable
          ariaLabel={"Acl requests"}
          columns={columns}
          rows={tableData}
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
  );
}

export default AclApprovals;
