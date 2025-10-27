import { Box, Button, Divider, DropdownMenu } from "@aivenio/aquarium";
import questionMark from "@aivenio/aquarium/dist/module/icons/questionMark";
import code from "@aivenio/aquarium/icons/code";
import codeBlock from "@aivenio/aquarium/icons/codeBlock";
import dataflow02 from "@aivenio/aquarium/icons/dataflow02";
import people from "@aivenio/aquarium/icons/people";
import { useNavigate } from "react-router-dom";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";
import { RequestsDropdown } from "src/app/layout/header/RequestsDropdown";
import { ProfileDropdown } from "src/app/layout/header/ProfileDropdown";
import { Routes } from "src/services/router-utils/types";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

const requestNewEntityPaths: { [key: string]: string } = {
  topic: Routes.TOPIC_REQUEST,
  connector: Routes.CONNECTOR_REQUEST,
  acl: Routes.ACL_REQUEST,
  schema: Routes.SCHEMA_REQUEST,
};

function HeaderNavigation() {
  const { isSuperAdminUser } = useAuthContext();
  const navigate = useNavigate();

  return (
    <Box display={"flex"} colGap={"l1"} alignItems="center">
      {!isSuperAdminUser && (
        <DropdownMenu
          onAction={(key) => {
            if (requestNewEntityPaths[key.toString()] !== undefined) {
              navigate(requestNewEntityPaths[key.toString()]);
            }
          }}
        >
          <DropdownMenu.Trigger>
            <Button.Dropdown aria-label="Request a new">
              Request a new
            </Button.Dropdown>
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
      )}

      <Box height={"l3"} paddingRight={"4"}>
        <Divider direction="vertical" size={1} />
      </Box>

      <nav aria-label={"Quick links"}>
        <Box
          component={"ul"}
          display={"flex"}
          colGap={"l2"}
          alignItems={"baseline"}
        >
          {!isSuperAdminUser && (
            <li>
              <RequestsDropdown />
            </li>
          )}
          <li>
            <ProfileDropdown />
          </li>
          <li>
            <HeaderMenuLink
              icon={questionMark}
              linkText={"Go to Klaw documentation page"}
              href={"https://www.klaw-project.io/docs"}
              rel={"noreferrer"}
            />
          </li>
        </Box>
      </nav>
    </Box>
  );
}

export default HeaderNavigation;
