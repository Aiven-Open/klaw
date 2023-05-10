import { Box, Grid, GridItem } from "@aivenio/aquarium";
import logo from "/src/app/layout/header/klaw_logo.png";

type BasePageProps = {
  headerContent?: JSX.Element;
  sidebar?: JSX.Element;
  content: JSX.Element;
};
function BasePage({ headerContent, content, sidebar }: BasePageProps) {
  return (
    <Grid
      colGap={"l1"}
      minHeight={"screen"}
      style={{
        gridTemplateColumns: "245px 1fr",
        gridTemplateRows: "auto 1fr",
      }}
    >
      <GridItem
        colStart={"1"}
        colEnd={"12"}
        height={"l5"}
        backgroundColor={"primary-80"}
        paddingX={"l2"}
      >
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
      </GridItem>
      {sidebar && (
        <GridItem colStart={"1"} colEnd={"2"} rowStart={"2"}>
          {sidebar}
        </GridItem>
      )}
      <GridItem colStart={sidebar ? "2" : "1"} colEnd={"12"}>
        <Box
          component={"main"}
          display={"flex"}
          flexDirection={"column"}
          padding={"l2"}
          width={"full"}
          style={{ isolation: "isolate" }}
        >
          {content}
        </Box>
      </GridItem>
    </Grid>
  );
}

export { BasePage };
