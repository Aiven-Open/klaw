import { Box, DropdownMenu, Button } from "@aivenio/aquarium";
import notifications from "@aivenio/aquarium/dist/module/icons/notifications";
import questionMark from "@aivenio/aquarium/dist/module/icons/questionMark";
import user from "@aivenio/aquarium/dist/module/icons/user";
import code from "@aivenio/aquarium/icons/code";
import codeBlock from "@aivenio/aquarium/icons/codeBlock";
import dataflow02 from "@aivenio/aquarium/icons/dataflow02";
import people from "@aivenio/aquarium/icons/people";
import { useNavigate } from "react-router-dom";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";
import { Routes } from "src/app/router_utils";

const requestNewEntityPaths: { [key: string]: string } = {
  topic: Routes.TOPIC_REQUEST,
  connector: Routes.CONNECTOR_REQUEST,
  acl: Routes.ACL_REQUEST,
  schema: Routes.SCHEMA_REQUEST,
};

function HeaderNavigation() {
  const navigate = useNavigate();

  return (
    <Box display={"flex"} colGap={"l2"} alignItems="center">
      <DropdownMenu
        onAction={(key) => {
          if (requestNewEntityPaths[key] !== undefined) {
            navigate(requestNewEntityPaths[key]);
          }
        }}
      >
        <DropdownMenu.Trigger>
          <Button.PrimaryDropdown aria-label="Request a new">
            Request a new
          </Button.PrimaryDropdown>
        </DropdownMenu.Trigger>
        <DropdownMenu.Items>
          <DropdownMenu.Item key="topic" icon={codeBlock}>
            Topic
          </DropdownMenu.Item>
          <DropdownMenu.Item key="acl" icon={people}>
            ACL
          </DropdownMenu.Item>
          <DropdownMenu.Item key="schema" icon={code}>
            Schema
          </DropdownMenu.Item>
          <DropdownMenu.Item key="connector" icon={dataflow02}>
            Kafka connector
          </DropdownMenu.Item>
        </DropdownMenu.Items>
      </DropdownMenu>

      <nav aria-label={"Quick links"}>
        <Box component={"ul"} display={"flex"} colGap={"l2"}>
          <li>
            <HeaderMenuLink
              icon={notifications}
              linkText={"Go to approve requests"}
              href={Routes.APPROVALS}
            />
          </li>
          <li>
            <HeaderMenuLink
              icon={questionMark}
              linkText={"Go to Klaw documentation page"}
              href={"https://www.klaw-project.io/docs"}
              rel={"noreferrer"}
            />
          </li>
          <li>
            <HeaderMenuLink
              icon={user}
              linkText={"Go to your profile"}
              href={`/myProfile`}
            />
          </li>
        </Box>
      </nav>
    </Box>
  );
}

export default HeaderNavigation;
