import { Flexbox, GridItem, Icon, Tooltip } from "@aivenio/design-system";
import questionMark from "@aivenio/design-system/dist/module/icons/questionMark";
import user from "@aivenio/design-system/dist/module/icons/user";
import notifications from "@aivenio/design-system/dist/module/icons/notifications";
import data from "@aivenio/design-system/dist/src/icons/console";

const originLocationKlawAngular = window.location.origin;

function createHeaderMenuListItem({
  icon,
  linkText,
  href,
  rel,
}: {
  icon: typeof data;
  linkText: string;
  href: string;
  rel?: string;
}) {
  return (
    <li style={{ color: "white" }}>
      <a href={href} rel={rel}>
        <span className={"visually-hidden"}>{linkText}</span>
        <span aria-hidden={"true"}>
          {/*DS does not fully support React18 now, where children */}
          {/*is not a default prop for FC*/}
          {/* eslint-disable-next-line @typescript-eslint/ban-ts-comment */}
          {/*@ts-ignore*/}
          <Tooltip content={linkText} placement="right">
            {/*@TODO add correct link*/}
            <Icon aria-hidden="true" icon={icon} fontSize={"20px"} />
          </Tooltip>
        </span>
      </a>
    </li>
  );
}

function Header() {
  return (
    <>
      <GridItem
        htmlTag={"header"}
        colStart={"1"}
        colEnd={"12"}
        height={"l5"}
        backgroundColor={"primary-80"}
        paddingX={"l2"}
      >
        <Flexbox
          height={"full"}
          justifyContent={"space-between"}
          alignItems={"center"}
          alignContent={"center"}
        >
          <a href={originLocationKlawAngular}>
            <span style={{ color: "white" }} className={"visually-hidden"}>
              Klaw homepage
            </span>
            <img
              aria-hidden="true"
              alt=""
              src="/klaw_logo.png"
              height={50}
              width={150}
            />
          </a>
          <nav aria-label={"Quick links"}>
            <Flexbox htmlTag={"ul"} colGap={"l2"}>
              {/*@TODO add correct link*/}
              {createHeaderMenuListItem({
                icon: notifications,
                href: `${originLocationKlawAngular}/execTopics`,
                linkText: "Go to approval requests",
              })}

              {createHeaderMenuListItem({
                icon: questionMark,
                href: "https://www.klaw-project.io/docs",
                linkText: "Go to Klaw documentation page",
                rel: "noreferrer",
              })}
              {createHeaderMenuListItem({
                icon: user,
                href: `${originLocationKlawAngular}/myProfile`,
                linkText: "Go to your profile",
              })}
            </Flexbox>
          </nav>
        </Flexbox>
      </GridItem>
    </>
  );
}

export default Header;
