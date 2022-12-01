import { Box, Grid } from "@aivenio/design-system";
import SidebarNavigation from "src/app/layout/SidebarNavigation";
import Header from "src/app/layout/Header";
import SkipLink from "src/app/layout/SkipLink";
import { useRef } from "react";

function Layout({ children }: { children: JSX.Element }) {
  const ref = useRef<HTMLDivElement>(null);
  return (
    <>
      <SkipLink mainContent={ref} />
      <Grid
        colGap={"l1"}
        minHeight={"screen"}
        style={{
          gridTemplateColumns: "240px 1fr",
          gridTemplateRows: "auto 1fr",
        }}
      >
        <Header />
        <SidebarNavigation />
        <Box component={"main"} padding={"l4"}>
          <div ref={ref}>{children}</div>
        </Box>
      </Grid>
    </>
  );
}

export default Layout;
