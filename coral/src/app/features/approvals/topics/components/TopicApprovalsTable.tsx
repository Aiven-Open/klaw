import {
  DataTable,
  DataTableColumn,
  GhostButton,
  Icon,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { UseMutateFunction } from "@tanstack/react-query";
import { Dispatch, SetStateAction, useState } from "react";
import { TopicRequest } from "src/domain/topic/topic-types";
import { GenericApiResponse, HTTPError } from "src/services/api";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";

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
  setDeclineModal: Dispatch<
    SetStateAction<{
      isOpen: boolean;
      topicId: number | null;
    }>
  >;
  approveRequest: UseMutateFunction<
    GenericApiResponse[],
    HTTPError,
    { requestEntityType: "TOPIC"; reqIds: string[] }
  >;
  quickActionLoading: boolean;
};
function TopicApprovalsTable(props: TopicApprovalsTableProp) {
  const {
    requests,
    setDetailsModal,
    setDeclineModal,
    approveRequest,
    quickActionLoading,
  } = props;

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
    {
      type: "status",
      field: "requestStatus",
      headerName: "Status",
      status: ({ requestStatus }) => {
        return {
          status: requestStatusChipStatusMap[requestStatus],
          text: requestStatusNameMap[requestStatus],
        };
      },
    },
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
      headerName: "Details",
      headerInvisible: true,
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
      headerName: "Approve",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: ({
        topicname,
        id,
        requestStatus,
      }: TopicRequestTableRow) => {
        const [isLoading, setIsLoading] = useState(false);
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => {
                setIsLoading(true);
                return approveRequest({
                  requestEntityType: "TOPIC",
                  reqIds: [String(id)],
                });
              }}
              title={`Approve topic request`}
              aria-label={`Approve topic request for ${topicname}`}
              disabled={quickActionLoading}
            >
              {isLoading && quickActionLoading ? (
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
      type: "custom",
      headerName: "Decline",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: ({
        id,
        requestStatus,
        topicname,
      }: TopicRequestTableRow) => {
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => setDeclineModal({ isOpen: true, topicId: id })}
              title={`Decline topic request`}
              aria-label={`Decline topic request for ${topicname}`}
              disabled={quickActionLoading}
            >
              <Icon color="grey-70" icon={deleteIcon} />
            </GhostButton>
          );
        }
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
