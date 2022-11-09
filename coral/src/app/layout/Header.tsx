import { Flexbox, Grid, IconButton } from "@aivenio/design-system";
import logOut from "@aivenio/design-system/dist/module/icons/logout";
import questionMark from "@aivenio/design-system/dist/module/icons/questionMark";
import { Link } from "react-router-dom";

const Header = () => {
  return (
    <Flexbox
      htmlTag="header"
      height={"l5"}
      justifyContent={"space-between"}
      alignItems={"center"}
      paddingX={"l1"}
    >
      <Link to="/">
        <img src="/klaw_logo_dark.png" height={50} width={150} />
      </Link>

      <Grid colGap={"l1"} cols={"2"}>
        <a
          href="https://www.klaw-project.io/docs"
          target="_blank"
          rel="noreferrer"
        >
          <IconButton
            icon={questionMark}
            aria-label="open documentation"
            tooltip={"Documentation"}
          />
        </a>
        <IconButton
          icon={logOut}
          aria-label="log out"
          tooltip={"Log out"}
          onClick={() => console.log("Log out")}
        />
      </Grid>
    </Flexbox>
  );
};

export default Header;
