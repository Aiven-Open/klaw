import { asButton, ButtonProps } from "@aivenio/aquarium";
import { Link, LinkProps } from "react-router-dom";
import { ResolveIntersectionTypes } from "types/utils";

const AsButton = asButton<"a", LinkProps, HTMLAnchorElement>(Link);

type InternalLinkButtonProps = ResolveIntersectionTypes<
  {
    children: string;
    kind?: ButtonProps["kind"];
  } & LinkProps
>;
function InternalLinkButton({ children, ...props }: InternalLinkButtonProps) {
  return <AsButton {...props}>{children}</AsButton>;
}

export { InternalLinkButton };
