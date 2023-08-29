import { Button, Tooltip } from "@aivenio/aquarium";
import { useState } from "react";
import { ResolveIntersectionTypes } from "types/utils";
import classes from "src/app/components/DisabledButtonTooltip.module.css";

/** DisabledButtonTooltip shows a disabled looking Primary Button
 * either with role link or button and a tooltip
 * this workaround is needed since DS tooltip do not work in react
 * strict mode at the moment. It also adds accessibility, which the
 * DS Button with tooltip does not provide (since it's disabled and
 * the tooltip text is not accessible for SR)
 **/
type DisabledButtonTooltipProps = ResolveIntersectionTypes<{
  children: string;
  tooltip: string;
  role?: "button" | "link";
}>;
function DisabledButtonTooltip({
  children,
  ...props
}: DisabledButtonTooltipProps) {
  const [tooltipOpen, setTooltipOpen] = useState(false);
  const toggleTooltip = () => setTooltipOpen(!tooltipOpen);
  const elementRole = props.role ? props.role : "button";

  return (
    <Tooltip
      // aria-hidden is currently not forwarded, but it is added for semantics purposes
      // while the tooltip has role "tooltip", it's not in the text
      // near the element, so screen reader won't be able to access the
      // information correctly. Also, the element related to the tooltip
      // does not build a relation by using aria-describedby.
      aria-hidden="true"
      content={props.tooltip}
      placement="bottom"
      isOpen={tooltipOpen}
      data-testid={"test"}
    >
      <div className={classes.buttonWrapper}>
        <Button.Primary
          role={elementRole}
          aria-disabled={true}
          aria-label={`${children}. ${props.tooltip}`}
          // this event handling is not ideal, since a button in focus will
          // behave unexpected when mouse and keyboard are used. since it's
          // an edge case and we (hopefully) can use tooltip soon, it is ok
          onMouseEnter={toggleTooltip}
          onMouseLeave={toggleTooltip}
          onFocus={toggleTooltip}
          onBlur={toggleTooltip}
          // eslint-disable-next-line @typescript-eslint/no-empty-function
          onClick={() => {}}
        >
          {children}
        </Button.Primary>
      </div>
    </Tooltip>
  );
}

export { DisabledButtonTooltip };
