import { Box, Flexbox, Icon } from "@aivenio/design-system";
import data from "@aivenio/design-system/dist/src/icons/console";
import classes from "src/app/layout/MainNavigationLink.module.css";
import { ReactElement, useState } from "react";
import caretDown from "@aivenio/design-system/dist/src/icons/caretDown";
import caretUp from "@aivenio/design-system/dist/src/icons/caretUp";
import MainNavigationLink from "src/app/layout/MainNavigationLink";

type MainNavigationSubmenuItemProps = {
  icon: typeof data;
  text: string;
  children: ReactElement<typeof MainNavigationLink>[];
  // this is a temp solution for "force" open the
  // sub-nave in our use-case. this will be
  // connected to routing later
  expanded?: boolean;
};

function MainNavigationSubmenuList(props: MainNavigationSubmenuItemProps) {
  const { icon, text, children, expanded = false } = props;
  const [open, setOpen] = useState<boolean>(expanded);

  const buttonText = open
    ? `${text} submenu, open. Click to close.`
    : `${text} submenu, closed. Click to open.`;

  return (
    <>
      <button
        className={classes.mainNavigationLink}
        aria-expanded={open ? "true" : "false"}
        onClick={() => setOpen(!open)}
      >
        <Icon
          icon={open ? caretUp : caretDown}
          style={{ position: "absolute", marginTop: "5px", marginLeft: "7px" }}
        />
        <span className={"visually-hidden"}>{buttonText}</span>
        <div aria-hidden={"true"}>
          <Flexbox
            direction={"row"}
            alignItems={"center"}
            colGap={"l1"}
            paddingLeft={"l3"}
            marginRight={"l2"}
          >
            <Icon icon={icon} />
            <span>{text}</span>
          </Flexbox>
        </div>
      </button>

      <Box marginTop={"l1"} aria-hidden={open ? "false" : "true"}>
        <Box
          component={"ul"}
          paddingLeft={"l3"}
          aria-label={`${text} submenu`}
          style={{ display: open ? "block" : "none" }}
        >
          {children.map((child, index) => {
            return (
              <li key={`${text.replaceAll(" ", "")}-${index}`}>{child}</li>
            );
          })}
        </Box>
      </Box>
    </>
  );
}

export default MainNavigationSubmenuList;
