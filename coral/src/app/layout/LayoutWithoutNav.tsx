import { useRef } from "react";
import { Outlet } from "react-router-dom";
import HeaderNavigation from "src/app/layout/header/HeaderNavigation";
import { BasePageWithoutNav } from "src/app/layout/page/BasePageWithoutNav";
import SkipLink from "src/app/layout/skip-link/SkipLink";

function LayoutWithoutNav() {
  const ref = useRef<HTMLDivElement>(null);
  return (
    <>
      <SkipLink mainContent={ref} />
      <BasePageWithoutNav
        headerContent={<HeaderNavigation />}
        content={
          <div ref={ref}>
            <Outlet />
          </div>
        }
      />
    </>
  );
}

export default LayoutWithoutNav;
