import { Flexbox, GridItem } from "@aivenio/design-system";
import questionMark from "@aivenio/design-system/dist/module/icons/questionMark";
import user from "@aivenio/design-system/dist/module/icons/user";
import notifications from "@aivenio/design-system/dist/module/icons/notifications";
import HeaderMenuLink from "src/app/layout/HeaderMenuLink";

function Header() {
  return (
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
        <a href={"/"}>
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
            {/*@TODO add correct link*/}
            <li>
              <HeaderMenuLink
                icon={user}
                linkText={"Go to your profile"}
                href={`/myProfile`}
              />
            </li>
          </Flexbox>
        </nav>
      </Flexbox>
    </GridItem>
  );
}

export default Header;
