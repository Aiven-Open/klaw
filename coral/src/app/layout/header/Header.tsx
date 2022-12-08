import { Flexbox } from "@aivenio/design-system";
import questionMark from "@aivenio/design-system/dist/module/icons/questionMark";
import user from "@aivenio/design-system/dist/module/icons/user";
import notifications from "@aivenio/design-system/dist/module/icons/notifications";
import HeaderMenuLink from "src/app/layout/header/HeaderMenuLink";

function Header() {
  return (
    <Flexbox
      htmlTag={"header"}
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
          src="/public/klaw_logo.png"
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
  );
}

export default Header;
