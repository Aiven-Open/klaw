import { Box, Icon } from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/console";
import classes from "src/app/layout/main-navigation/MainNavigationSubmenuList.module.css";
import { ReactElement, useState } from "react";
import caretDown from "@aivenio/aquarium/dist/src/icons/caretDown";
import caretUp from "@aivenio/aquarium/dist/src/icons/caretUp";
import MainNavigationLink from "src/app/layout/main-navigation/MainNavigationLink";

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
        className={classes.mainNavigationSubmenuButton}
        aria-expanded={open ? "true" : "false"}
        onClick={() => setOpen(!open)}
      >
        <Icon
          icon={open ? caretUp : caretDown}
          style={{ position: "absolute", marginTop: "5px", marginLeft: "2px" }}
        />
        <span className={"visually-hidden"}>{buttonText}</span>

        <Box
          display={"flex"}
          aria-hidden={"true"}
          flexDirection={"row"}
          alignItems={"center"}
          colGap={"l1"}
          paddingLeft={"l2"}
          marginRight={"l2"}
        >
          <Icon icon={icon} />
          <span>{text}</span>
        </Box>
      </button>

      <Box marginTop={"l1"} aria-hidden={open ? "false" : "true"}>
        {open && (
          <Box
            component={"ul"}
            paddingLeft={"l3"}
            aria-label={`${text} submenu`}
          >
            {children.map((child, index) => {
              return (
                <li key={`${text.replaceAll(" ", "")}-${index}`}>{child}</li>
              );
            })}
          </Box>
        )}
      </Box>
    </>
  );
}

export default MainNavigationSubmenuList;
