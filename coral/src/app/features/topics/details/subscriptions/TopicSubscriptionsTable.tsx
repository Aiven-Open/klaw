import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  Skeleton,
  StatusChip,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { LoadingTable } from "src/app/features/components/layouts/LoadingTable";

type SubscriptionOptions =
  | "aclInfoList"
  | "prefixedAclInfoList"
  | "transactionalAclInfoList";

interface TopicSubscriptionsTableProps {
  isUpdating: boolean;
  selectedSubs: SubscriptionOptions;
  filteredData: AclOverviewInfo[];
  onDelete: (req_no: string) => void;
  onDetails: ({
    isAivenCluster,
    aclReqNo,
    consumerGroupId,
  }: {
    isAivenCluster: boolean;
    aclReqNo: string;
    consumerGroupId?: string;
  }) => void;
}

interface AclInfoListRow {
  id: AclOverviewInfo["req_no"];
  principals: string[];
  ips: string[];
  aclType: AclOverviewInfo["topictype"];
  team: AclOverviewInfo["teamname"];
  showDeleteAcl: AclOverviewInfo["showDeleteAcl"];
  topicname?: AclOverviewInfo["topicname"];
  transactionalId?: AclOverviewInfo["transactionalId"];
  consumerGroupId?: AclOverviewInfo["consumergroup"];
  kafkaFlavorType?: AclOverviewInfo["kafkaFlavorType"];
}

const getRows = (
  selectedSubs: SubscriptionOptions,
  data: AclOverviewInfo[]
): AclInfoListRow[] => {
  return data.map(
    ({
      req_no,
      acl_ssl,
      acl_ip,
      topictype,
      teamname,
      showDeleteAcl,
      topicname,
      transactionalId,
      consumergroup,
      kafkaFlavorType,
    }) => {
      const row: AclInfoListRow = {
        id: req_no,
        principals: acl_ssl ? [acl_ssl] : [],
        ips: acl_ip ? [acl_ip] : [],
        aclType: topictype,
        team: teamname,
        showDeleteAcl,
        consumerGroupId: consumergroup,
        kafkaFlavorType: kafkaFlavorType,
      };

      if (selectedSubs === "prefixedAclInfoList") {
        row.topicname = topicname;
      }

      if (selectedSubs === "transactionalAclInfoList") {
        row.transactionalId = transactionalId || "Not found";
      }

      return row;
    }
  );
};

const getColumns = (
  selectedSubs: SubscriptionOptions,
  onDelete: TopicSubscriptionsTableProps["onDelete"],
  onDetails: TopicSubscriptionsTableProps["onDetails"]
): Array<DataTableColumn<AclInfoListRow>> => {
  const additionalColumns: {
    [key in
      | "prefixedAclInfoList"
      | "transactionalAclInfoList"]: DataTableColumn<AclInfoListRow>;
  } = {
    prefixedAclInfoList: {
      type: "text",
      field: "topicname",
      headerName: "Prefix",
    },
    transactionalAclInfoList: {
      type: "text",
      field: "transactionalId",
      headerName: "Transactional ID",
    },
  };

  const columns: Array<DataTableColumn<AclInfoListRow>> = [
    {
      type: "custom",
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ principals }: AclInfoListRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {principals.map((principal, index) => (
              <li key={`${principal}-${index}`}>
                <StatusChip dense status="neutral" text={principal} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "custom",
      headerName: "IP addresses",
      UNSAFE_render: ({ ips }: AclInfoListRow) => {
        if (ips.length === 0) {
          return "*";
        }
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {ips.map((ip, index) => (
              <li key={`${ip}-${index}`}>
                <StatusChip dense status="neutral" text={ip} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "status",
      headerName: "ACL type",
      status: ({ aclType }) => ({
        status: aclType === "Consumer" ? "success" : "info",
        text: aclType.toUpperCase(),
      }),
    },
    {
      type: "text",
      field: "team",
      headerName: "Team",
    },
    {
      type: "action",
      headerName: "Details",
      headerInvisible: true,
      width: 30,
      action: ({ consumerGroupId, kafkaFlavorType, id }) => ({
        text: "Details",
        icon: infoIcon,
        onClick: () => {
          onDetails({
            consumerGroupId,
            aclReqNo: id,
            isAivenCluster:
              kafkaFlavorType !== undefined &&
              kafkaFlavorType === "AIVEN_FOR_APACHE_KAFKA",
          });
        },
        "aria-label": `Show details of request ${id}`,
      }),
    },
    {
      type: "action",
      headerName: "Delete",
      headerInvisible: true,
      width: 30,
      action: ({ id, showDeleteAcl }) => ({
        text: "Delete",
        icon: deleteIcon,
        onClick: () => onDelete(id),
        disabled: !showDeleteAcl,
        "aria-label": `Create deletion request for request ${id}`,
      }),
    },
  ];

  if (
    selectedSubs === "prefixedAclInfoList" ||
    selectedSubs === "transactionalAclInfoList"
  ) {
    columns.unshift(additionalColumns[selectedSubs]);
  }

  return columns;
};

export const TopicSubscriptionsTable = ({
  selectedSubs,
  filteredData,
  onDelete,
  onDetails,
  isUpdating,
}: TopicSubscriptionsTableProps) => {
  const rows = getRows(selectedSubs, filteredData);
  const columns = getColumns(selectedSubs, onDelete, onDetails);
  const loadingColumns = columns.map((col) => {
    return {
      headerName: col.headerName,
      width: col.width,
      headerVisible: col.headerInvisible,
    };
  });

  if (isUpdating) {
    const loadingRowLength = rows.length === 0 ? 1 : rows.length;
    return (
      <LoadingTable rowLength={loadingRowLength} columns={loadingColumns} />
    );
  }

  if (rows.length === 0) {
    return (
      <EmptyState title="No subscriptions">
        No subscription matched your criteria.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={"Topic subscriptions"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};
