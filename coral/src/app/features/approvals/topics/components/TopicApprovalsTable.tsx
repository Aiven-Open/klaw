import {
  Box,
  DataTable,
  DataTableColumn,
  GhostButton,
  Icon,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import { TopicRequestNew } from "src/domain/topic/topic-types";

interface TopicRequestTableData {
  id: number; // unclear
  topicname: string;
  environmentName: string;
  topictype: string;
  teamname: string; // unclear
  requestor: string;
  requesttimestring: string;
}

const columns: Array<DataTableColumn<TopicRequestTableData>> = [
  { type: "text", field: "topicname", headerName: "Topic" },
  { type: "text", field: "environmentName", headerName: "Cluster" },
  { type: "text", field: "topictype", headerName: "Type" },
  { type: "text", field: "teamname", headerName: "Claim by team" },
  { type: "text", field: "requestor", headerName: "Requested by" },
  {
    type: "text",
    field: "requesttimestring",
    headerName: "Date Requested",
    formatter: (value) => {
      return `${value} UTC`;
    },
  },
  {
    type: "custom",
    // @TODO PR open in DS to be able to
    // add an "invisible" header name that
    // is used as aria-label. That will also
    // solve the duplicate key warning
    //https://github.com/aiven/design-system/pull/950
    headerName: "Details",
    width: 30,
    UNSAFE_render: (row) => {
      return (
        <GhostButton
          onClick={() => alert("Approve")}
          title={`View topic request `}
          dense
        >
          <Box
            component={"span"}
            display={"flex"}
            alignItems={"center"}
            colGap={"2"}
          >
            <Icon icon={infoSign} />
            <span aria-hidden={"true"}>View details</span>
            <span className={"visually-hidden"}>
              View topic request for {row.topicname}
            </span>
          </Box>
        </GhostButton>
      );
    },
  },
  {
    type: "custom",
    // @TODO PR open in DS to be able to
    // add an "invisible" header name that
    // is used as aria-label. That will also
    // solve the duplicate key warning
    //https://github.com/aiven/design-system/pull/950
    headerName: "Approve",
    width: 30,
    UNSAFE_render: (row) => {
      return (
        <GhostButton
          onClick={() => alert("Approve")}
          title={`Approve topic request for ${row.topicname}`}
        >
          <Icon color="grey-70" icon={tickCircle} />
        </GhostButton>
      );
    },
  },
  {
    type: "custom",
    // @TODO PR open in DS to be able to
    // add an "invisible" header name that
    // is used as aria-label. That will also
    // solve the duplicate key warning
    //https://github.com/aiven/design-system/pull/950
    headerName: "Decline",
    width: 30,
    UNSAFE_render: (row) => {
      return (
        <GhostButton
          onClick={() => alert("Decline")}
          title={`Decline topic request for ${row.topicname}`}
        >
          <Icon color="grey-70" icon={deleteIcon} />
        </GhostButton>
      );
    },
  },
];

type TopicApprovalsTableProp = {
  requests: TopicRequestNew[];
};
function TopicApprovalsTable(props: TopicApprovalsTableProp) {
  const { requests } = props;

  const rows: TopicRequestTableData[] = requests.map(
    (request: TopicRequestNew) => {
      return {
        id: request.topicid,
        topicname: request.topicname,
        environmentName: request.environmentName,
        topictype: request.topictype,
        teamname: request.teamname,
        requestor: request.requestor,
        requesttimestring: request.requesttimestring,
      };
    }
  );
  return (
    <DataTable
      ariaLabel={"Topic requests"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { TopicApprovalsTable };
