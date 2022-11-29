import { Grid } from "@aivenio/design-system";
import SidebarNavigation from "src/app/layout/SidebarNavigation";
import Header from "src/app/layout/Header";

function Layout({ children }: { children: JSX.Element }) {
  return (
    <Grid
      colGap={"l1"}
      minHeight={"screen"}
      style={{
        gridTemplateColumns: "minmax(200px, 350px) 1fr",
        gridTemplateRows: "auto 1fr",
      }}
    >
      <Header />
      <SidebarNavigation />
      <main className={"p-6"}>{children}</main>
    </Grid>
  );
}

export default Layout;
