import classes from "src/app/layout/SkipLink.module.css";
import { MouseEvent, RefObject } from "react";

type SkipLinkProps = {
  mainContent: RefObject<HTMLDivElement>;
};

function SkipLink(props: SkipLinkProps) {
  const { mainContent } = props;
  function scrollIntoView(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    if (mainContent.current) {
      mainContent.current.scrollIntoView();
      mainContent.current.tabIndex = -1;
      mainContent.current.focus();
    }
  }

  return (
    <button
      type={"button"}
      onClick={scrollIntoView}
      className={classes.skipLink}
    >
      Skip to main content
    </button>
  );
}

export default SkipLink;
