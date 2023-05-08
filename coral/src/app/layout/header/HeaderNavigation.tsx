import { Box } from "@aivenio/aquarium";
import questionMark from "@aivenio/aquarium/dist/module/icons/questionMark";
import user from "@aivenio/aquarium/dist/module/icons/user";
import notifications from "@aivenio/aquarium/dist/module/icons/notifications";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";

function HeaderNavigation() {
  return (
    <nav aria-label={"Quick links"}>
      <Box component={"ul"} display={"flex"} colGap={"l2"}>
        <li>
          <HeaderMenuLink
            icon={notifications}
            linkText={"Go to approval requests"}
            href={`/execTopics`}
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
  );
}

export default HeaderNavigation;
