import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import SkipLink from "src/app/layout/skip-link/SkipLink";
import { ReactNode, useRef } from "react";
import { BasePage } from "src/app/layout/page/BasePage";

function Layout({ children }: { children: ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);

  return (
    <>
      <SkipLink mainContent={ref} />
      <BasePage
        headerContent={<HeaderNavigation />}
        content={<div ref={ref}>{children}</div>}
        sidebar={<MainNavigation />}
      />
    </>
  );
}

export default Layout;
