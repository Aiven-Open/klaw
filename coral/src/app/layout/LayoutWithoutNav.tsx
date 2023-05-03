import { Grid, GridItem } from "@aivenio/aquarium";
import React, { useRef } from "react";
import Header from "src/app/layout/header/Header";
import SkipLink from "src/app/layout/skip-link/SkipLink";

function LayoutWithoutNav({ children }: React.PropsWithChildren) {
  const ref = useRef<HTMLDivElement>(null);
  return (
    <>
      <SkipLink mainContent={ref} />
      <Grid
        colGap={"l1"}
        rowGap={"l2"}
        minHeight={"screen"}
        style={{
          gridTemplateColumns: "auto",
          gridTemplateRows: "auto 1fr",
          isolation: "isolate",
        }}
      >
        <GridItem height={"l5"} backgroundColor={"primary-80"} paddingX={"l2"}>
          <Header />
        </GridItem>
        <GridItem justifySelf={"center"} width={"1/2"}>
          <div ref={ref}>{children}</div>
        </GridItem>
      </Grid>
    </>
  );
}

export default LayoutWithoutNav;
