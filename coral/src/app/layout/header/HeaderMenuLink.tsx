import { Icon, Tooltip } from "@aivenio/design-system";
import data from "@aivenio/design-system/dist/src/icons/console";

type HeaderMenuLinkProps = {
  icon: typeof data;
  linkText: string;
  href: string;
  rel?: string;
};
function HeaderMenuLink(props: HeaderMenuLinkProps) {
  const { icon, linkText, href, rel } = props;
  return (
    <a href={href} rel={rel} style={{ color: "white" }}>
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
  );
}

export default HeaderMenuLink;
