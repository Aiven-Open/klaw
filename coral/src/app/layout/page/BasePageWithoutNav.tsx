import { Box, Grid } from "@aivenio/aquarium";
import logo from "/src/app/layout/header/klaw_logo.png";

type BasePageWithoutNavProps = {
  headerContent?: JSX.Element;
  content: JSX.Element;
};
function BasePageWithoutNav({
  headerContent,
  content,
}: BasePageWithoutNavProps) {
  return (
    <Grid
      minHeight={"screen"}
      style={{
        gridTemplateColumns: "1fr",
        gridTemplateRows: "auto 1fr",
      }}
    >
      <Grid.Item height={"l5"} backgroundColor={"primary-80"} paddingX={"l2"}>
        <Box
          component={"header"}
          display={"flex"}
          height={"full"}
          justifyContent={"space-between"}
          alignItems={"center"}
          alignContent={"center"}
        >
          <a href={"/"}>
            <span style={{ color: "white" }} className={"visually-hidden"}>
              Klaw homepage
            </span>
            <img aria-hidden="true" alt="" src={logo} height={50} width={150} />
          </a>
          {headerContent}
        </Box>
      </Grid.Item>
      <Grid.Item>
        <Box
          component={"main"}
          display={"flex"}
          flexDirection={"column"}
          padding={"l2"}
          style={{ isolation: "isolate" }}
          justifyContent={"center"}
          width={"3/5"}
          margin={"auto"}
        >
          {content}
        </Box>
      </Grid.Item>
    </Grid>
  );
}

export { BasePageWithoutNav };
