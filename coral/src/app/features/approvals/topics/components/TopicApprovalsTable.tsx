import {
  DataTable,
  DataTableColumn,
  GhostButton,
  Icon,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { Dispatch, SetStateAction } from "react";
import { TopicRequest } from "src/domain/topic/topic-types";

interface TopicRequestTableRow {
  id: TopicRequest["topicid"];
  topicname: TopicRequest["topicname"];
  environmentName: TopicRequest["environmentName"];
  requestStatus: TopicRequest["requestStatus"];
  teamname: TopicRequest["teamname"];
  requestor: TopicRequest["requestor"];
  requesttimestring: TopicRequest["requesttimestring"];
}

type TopicApprovalsTableProp = {
  requests: TopicRequest[];
  setDetailsModal: Dispatch<
    SetStateAction<{
      isOpen: boolean;
      topicId: number | null;
    }>
  >;
};
function TopicApprovalsTable(props: TopicApprovalsTableProp) {
  const { requests, setDetailsModal } = props;

  const columns: Array<DataTableColumn<TopicRequestTableRow>> = [
    { type: "text", field: "topicname", headerName: "Topic" },
    {
      type: "status",
      field: "environmentName",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
    { type: "text", field: "requestStatus", headerName: "Status" },
    { type: "text", field: "teamname", headerName: "Claim by team" },
    { type: "text", field: "requestor", headerName: "Requested by" },
    {
      type: "text",
      field: "requesttimestring",
      headerName: "Requested on",
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
      headerName: "",
      width: 30,
      UNSAFE_render: ({ id, topicname }: TopicRequestTableRow) => {
        return (
          <GhostButton
            onClick={() => setDetailsModal({ isOpen: true, topicId: id })}
            icon={infoSign}
            dense
          >
            <span aria-hidden={"true"}>View details</span>
            <span className={"visually-hidden"}>
              View topic request for {topicname}
            </span>
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
      headerName: "",
      width: 30,
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Approve")}
            aria-label={`Approve topic request for ${row.topicname}`}
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
      headerName: "",
      width: 30,
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Decline")}
            aria-label={`Decline topic request for ${row.topicname}`}
          >
            <Icon color="grey-70" icon={deleteIcon} />
          </GhostButton>
        );
      },
    },
  ];

  const rows: TopicRequestTableRow[] = requests.map((request: TopicRequest) => {
    return {
      id: request.topicid,
      topicname: request.topicname,
      environmentName: request.environmentName,
      requestStatus: request.requestStatus,
      teamname: request.teamname,
      requestor: request.requestor,
      requesttimestring: request.requesttimestring,
    };
  });

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
