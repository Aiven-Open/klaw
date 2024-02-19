import { Badge, Button, DropdownMenu, Icon } from "@aivenio/aquarium";
import code from "@aivenio/aquarium/icons/code";
import codeBlock from "@aivenio/aquarium/icons/codeBlock";
import dataflow02 from "@aivenio/aquarium/icons/dataflow02";
import notifications from "@aivenio/aquarium/icons/notifications";
import people from "@aivenio/aquarium/icons/people";
import { useNavigate } from "react-router-dom";
import { usePendingRequests } from "src/app/hooks/usePendingRequests";
import {
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  Routes,
} from "src/services/router-utils/types";

const approveRequestsPaths: { [key: string]: string } = {
  topic: `${Routes.APPROVALS}/${
    APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.TOPICS]
  }`,
  acl: `${Routes.APPROVALS}/${
    APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.ACLS]
  }`,
  schema: `${Routes.APPROVALS}/${
    APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.SCHEMAS]
  }`,
  connector: `${Routes.APPROVALS}/${
    APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.CONNECTORS]
  }`,
};

function RequestsDropdown() {
  const navigate = useNavigate();
  const { TOTAL_NOTIFICATIONS, TOPIC, ACL, CONNECTOR, SCHEMA } =
    usePendingRequests();

  return (
    <DropdownMenu
      onAction={(key) => {
        if (approveRequestsPaths[key.toString()] !== undefined) {
          navigate(approveRequestsPaths[key.toString()]);
        }
      }}
    >
      <DropdownMenu.Trigger>
        <Button.Ghost
          aria-label={
            TOTAL_NOTIFICATIONS !== undefined && TOTAL_NOTIFICATIONS > 0
              ? `See ${TOTAL_NOTIFICATIONS} pending requests`
              : `No pending requests`
          }
        >
          {TOTAL_NOTIFICATIONS !== undefined && TOTAL_NOTIFICATIONS > 0 ? (
            <Badge.Notification>
              <Icon icon={notifications} fontSize={"20px"} color={"grey-0"} />
            </Badge.Notification>
          ) : (
            <Icon icon={notifications} fontSize={"20px"} color={"grey-0"} />
          )}
        </Button.Ghost>
      </DropdownMenu.Trigger>
      <DropdownMenu.Items>
        <DropdownMenu.Item key="topic" icon={codeBlock}>
          {`${TOPIC === 0 ? "No" : TOPIC} pending topic requests`}
        </DropdownMenu.Item>
        <DropdownMenu.Item key="acl" icon={people}>
          {`${ACL === 0 ? "No" : ACL} pending ACL requests`}
        </DropdownMenu.Item>
        <DropdownMenu.Item key="schema" icon={code}>
          {`${SCHEMA === 0 ? "No" : SCHEMA} pending schema requests`}
        </DropdownMenu.Item>
        <DropdownMenu.Item key="connector" icon={dataflow02}>
          {`${
            CONNECTOR === 0 ? "No" : CONNECTOR
          } pending Kafka connector requests`}
        </DropdownMenu.Item>
      </DropdownMenu.Items>
    </DropdownMenu>
  );
}

export { RequestsDropdown };
