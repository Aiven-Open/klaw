import { Flexbox, Icon, GridItem, Tooltip } from "@aivenio/design-system";
import questionMark from "@aivenio/design-system/dist/module/icons/questionMark";
import user from "@aivenio/design-system/dist/module/icons/user";
import notifications from "@aivenio/design-system/dist/module/icons/notifications";

// This is a WIP placeholder
// that does not have real data yet
function Header() {
  return (
    <GridItem
      htmlTag={"header"}
      colStart={"1"}
      colEnd={"12"}
      height={"l5"}
      backgroundColor={"primary-80"}
      style={{ color: "white" }}
      paddingX={"l2"}
    >
      {/*@TODO check of Flexbox can hava a aria-label attribute*/}

      <Flexbox
        height={"full"}
        justifyContent={"space-between"}
        alignItems={"center"}
        alignContent={"center"}
      >
        <a href="/">
          <span className={"visually-hidden"}>Klaw homepage</span>
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
            <li>
              {/*@TODO add correct link*/}

              <a href={"/"} className={"typography-body-large"}>
                {/*@TODO replace with IconButton when color change possible*/}
                <span className={"visually-hidden"}>Approval requests</span>
                <span aria-hidden={"true"}>
                  {/*@TODO find out what happens here*/}
                  {/* eslint-disable-next-line @typescript-eslint/ban-ts-comment */}
                  {/*@ts-ignore*/}
                  <Tooltip content="Tooltip content" placement="right">
                    <Icon aria-hidden="true" icon={notifications} />
                  </Tooltip>
                </span>
              </a>
            </li>
            <li>
              <a
                href="https://www.klaw-project.io/docs"
                rel="noreferrer"
                className={"typography-body-large"}
              >
                {/*@TODO replace with IconButton when color change possible*/}
                <span className={"visually-hidden"}>
                  Go to Klaw documentation page
                </span>
                <span aria-hidden={"true"}>
                  {/*@TODO find out what happens here*/}
                  {/* eslint-disable-next-line @typescript-eslint/ban-ts-comment */}
                  {/*@ts-ignore*/}
                  <Tooltip content="Tooltip content" placement="right">
                    <Icon aria-hidden="true" icon={questionMark} />
                  </Tooltip>
                </span>
              </a>
            </li>
            <li>
              <a href="/" className={"typography-body-large"}>
                {/*@TODO replace with IconButton when color change possible*/}
                <span className={"visually-hidden"}>Your Profile</span>

                <span aria-hidden={"true"}>
                  {/*@TODO find out what happens here*/}
                  {/* eslint-disable-next-line @typescript-eslint/ban-ts-comment */}
                  {/*@ts-ignore*/}
                  <Tooltip content="Tooltip content" placement="right">
                    {/*@TODO add correct link*/}
                    <Icon aria-hidden="true" icon={user} />
                  </Tooltip>
                </span>
              </a>
            </li>
          </Flexbox>
        </nav>
      </Flexbox>
    </GridItem>
  );
}

export default Header;
