import Header from "src/app/layout/Header";
import SideNavigation from "src/app/layout/SideNavigation";
import { Grid, GridItem } from "@aivenio/design-system";

const Layout = ({ children }: { children: JSX.Element }) => {
  return (
    <Grid
      height={"full"}
      colGap={"l1"}
      style={{
        gridTemplateColumns: "minmax(200px, 350px) 1fr",
        gridTemplateRows: "auto 1fr",
      }}
    >
      <GridItem colStart={"1"} colEnd={"12"}>
        <Header />
      </GridItem>
      <SideNavigation />
      <main>{children}</main>
    </Grid>
  );
};

export default Layout;
