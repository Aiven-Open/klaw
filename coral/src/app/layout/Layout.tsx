import { Box, Grid, GridItem } from "@aivenio/aquarium";
import { useRef } from "react";
import { Outlet } from "react-router-dom";
import Header from "src/app/layout/header/Header";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import SkipLink from "src/app/layout/skip-link/SkipLink";

function Layout() {
  const ref = useRef<HTMLDivElement>(null);
  return (
    <>
      <SkipLink mainContent={ref} />
      <Grid
        colGap={"l1"}
        minHeight={"screen"}
        style={{
          gridTemplateColumns: "245px 1fr",
          gridTemplateRows: "auto 1fr",
          isolation: "isolate",
        }}
      >
        <GridItem
          colStart={"1"}
          colEnd={"12"}
          height={"l5"}
          backgroundColor={"primary-80"}
          paddingX={"l2"}
        >
          <Header />
        </GridItem>
        <GridItem colStart={"1"} colEnd={"2"} rowStart={"2"}>
          <MainNavigation />
        </GridItem>
        <GridItem colStart={"2"} colEnd={"12"}>
          <Box
            component={"main"}
            display={"flex"}
            flexDirection={"column"}
            padding={"l2"}
            width={"full"}
          >
            <div ref={ref}>
              <Outlet />
            </div>
          </Box>
        </GridItem>
      </Grid>
    </>
  );
}

export default Layout;
