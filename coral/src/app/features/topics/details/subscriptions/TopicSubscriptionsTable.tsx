import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  StatusChip,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import { AclOverviewInfo } from "src/domain/topic/topic-types";

type SubscriptionOptions =
  | "aclInfoList"
  | "prefixedAclInfoList"
  | "transactionalAclInfoList";

interface TopicSubscriptionsTableProps {
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
          <Box display="flex" wrap={"wrap"} gap={"2"}>
            {principals.map((principal, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${principal}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${principal} `}
              />
            ))}
          </Box>
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
          <Box display="flex" wrap={"wrap"} gap={"2"}>
            {ips.map((ip, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${ip}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${ip} `}
              />
            ))}
          </Box>
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
}: TopicSubscriptionsTableProps) => {
  const rows = getRows(selectedSubs, filteredData);
  const columns = getColumns(selectedSubs, onDelete, onDetails);

  return rows.length === 0 ? (
    <EmptyState title="No subscriptions">
      No subscription matched your criteria.
    </EmptyState>
  ) : (
    <DataTable
      ariaLabel={"Topic subscriptions"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};
