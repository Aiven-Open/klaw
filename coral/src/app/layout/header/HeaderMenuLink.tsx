import { Icon, Tooltip } from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/console";
import { useState } from "react";
import { Link } from "react-router-dom";

type HeaderMenuLinkProps = {
  icon: typeof data;
  linkText: string;
  href: string;
  rel?: string;
};
function HeaderMenuLink(props: HeaderMenuLinkProps) {
  const { icon, linkText, href, rel } = props;

  const [isOpen, setIsOpen] = useState(false);
  const toggleOpen = () => setIsOpen(!isOpen);

  return (
    <Link
      to={href}
      rel={rel}
      // These mouse events are necessary because the Tooltip component does not show the popover on hover with React version >= 18
      // @TODO: remove when aquarium is compatible with React version >= 18
      onMouseEnter={toggleOpen}
      onMouseLeave={toggleOpen}
      // Allow displaying the tooltip when navigating with keyboard
      // Because the Tooltip is rendered outside the main DOM hierarchy, it is ignored by screen readers
      // So we can display it with keyboard navigation for users who use keyboard navigation, but not a screen reader
      onFocus={toggleOpen}
      onBlur={toggleOpen}
    >
      <span className={"visually-hidden"}>{linkText}</span>
      {/*Aquarium does not fully support React 18 now, where children */}
      {/*is not a default prop for FC*/}
      {/* eslint-disable-next-line @typescript-eslint/ban-ts-comment */}
      {/*@ts-ignore*/}
      <Tooltip
        // aria-hidden is currently not forwarded, but it is added for semantics purposes
        aria-hidden="true"
        content={linkText}
        placement="right"
        isOpen={isOpen}
      >
        {/* aria-hidden="true" is added natively to the Icon component */}
        <Icon icon={icon} fontSize={"20px"} color={"grey-0"} />
      </Tooltip>
    </Link>
  );
}

export default HeaderMenuLink;
