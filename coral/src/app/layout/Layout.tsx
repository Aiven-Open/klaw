import { useRef } from "react";
import { Outlet } from "react-router-dom";
import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import MainNavigation from "src/app/layout/main-navigation/MainNavigation";
import { BasePage } from "src/app/layout/page/BasePage";
import SkipLink from "src/app/layout/skip-link/SkipLink";

function Layout() {
  const ref = useRef<HTMLDivElement>(null);

  return (
    <>
      <SkipLink mainContent={ref} />
      <BasePage
        headerContent={<HeaderNavigation />}
        content={
          <div ref={ref}>
            <Outlet />
          </div>
        }
        sidebar={<MainNavigation />}
      />
    </>
  );
}

export default Layout;
