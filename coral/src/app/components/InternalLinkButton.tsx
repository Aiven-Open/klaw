import { asButton, ButtonProps, Button } from "@aivenio/aquarium";
import { Link, LinkProps } from "react-router-dom";
import { ResolveIntersectionTypes } from "types/utils";
import { ReactNode } from "react";

const AsButton = asButton<"a", LinkProps, HTMLAnchorElement>(Link);

type InternalLinkButtonProps = ResolveIntersectionTypes<
  {
    children: string | ReactNode;
    kind?: ButtonProps["kind"];
    disabled?: boolean;
  } & LinkProps
>;
function InternalLinkButton({ children, ...props }: InternalLinkButtonProps) {
  if (props.disabled) {
    // a Link cannot really be disabled semantically. Adding an empty ref
    // with aria-disabled would solve this, but that there are no styles
    // from DS to cover this, so we're showing a disabled button with role
    // link in this case instead to provide the best feedback for assistive technology
    return (
      <Button
        role={"link"}
        aria-disabled={true}
        disabled={true}
        kind={props.kind}
      >
        {children}
      </Button>
    );
  }
  return <AsButton {...props}>{children}</AsButton>;
}

export { InternalLinkButton };
