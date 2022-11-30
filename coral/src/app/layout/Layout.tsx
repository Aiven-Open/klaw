import { Box, Grid } from "@aivenio/design-system";
import SidebarNavigation from "src/app/layout/SidebarNavigation";
import Header from "src/app/layout/Header";

function Layout({ children }: { children: JSX.Element }) {
  return (
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
      {/*ID is used to provide a skip link*/}
      <Box component={"main"} padding={"l4"} id={"main-content"}>
        {children}
      </Box>
    </Grid>
  );
}

export default Layout;
