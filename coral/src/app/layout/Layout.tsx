import { Box, Grid, GridItem } from "@aivenio/aquarium";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import Header from "src/app/layout/header/Header";
import SkipLink from "src/app/layout/skip-link/SkipLink";
import { ReactNode, useRef } from "react";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { Outlet } from "react-router-dom";

function Layout() {
  const authUser = useAuthContext();

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
          <MainNavigation authUser={authUser} />
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
