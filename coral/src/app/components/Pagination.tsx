import { Flexbox, GhostButton, Icon } from "@aivenio/design-system";
import chevronBackward from "@aivenio/design-system/dist/src/icons/chevronBackward";
import chevronLeft from "@aivenio/design-system/dist/src/icons/chevronLeft";
import chevronRight from "@aivenio/design-system/dist/src/icons/chevronRight";
import chevronForward from "@aivenio/design-system/dist/src/icons/chevronForward";

type PaginationProps = {
  activePage: number;
  totalPages: number;
  setActivePage: (pageNumber: number) => void;
};

function Pagination(props: PaginationProps) {
  const { activePage, totalPages, setActivePage } = props;

  function onUpdatePage(nextPageNumber: number) {
    setActivePage(nextPageNumber);
  }

  const currentPageIsFirstPage = activePage === 1;
  const currentPageIsLastPage = activePage === totalPages;

  return (
    <nav
      role="navigation"
      aria-label={`Pagination navigation, you're on page ${activePage} of ${totalPages}`}
    >
      <Flexbox htmlTag={"ul"} colGap={"l1"} alignItems={"center"}>
        <li aria-hidden={currentPageIsFirstPage}>
          <GhostButton
            disabled={currentPageIsFirstPage}
            onClick={() => onUpdatePage(1)}
          >
            <span className={"visually-hidden"}>Go to first page</span>
            <Icon aria-hidden={true} icon={chevronBackward} />
          </GhostButton>
        </li>
        <li aria-hidden={currentPageIsFirstPage}>
          <GhostButton
            disabled={currentPageIsFirstPage}
            onClick={() => onUpdatePage(activePage - 1)}
          >
            <span className={"visually-hidden"}>
              Go to previous page, page {activePage - 1}
            </span>
            <Icon aria-hidden={true} icon={chevronLeft} />
          </GhostButton>
        </li>
        <li aria-hidden={true}>
          Page {activePage} of {totalPages}
        </li>
        <li aria-hidden={currentPageIsLastPage}>
          <GhostButton
            disabled={currentPageIsLastPage}
            onClick={() => onUpdatePage(activePage + 1)}
          >
            <span className={"visually-hidden"}>
              Go to next page, page {activePage + 1}
            </span>
            <Icon aria-hidden={true} icon={chevronRight} />
          </GhostButton>
        </li>
        <li aria-hidden={currentPageIsLastPage}>
          <GhostButton
            disabled={currentPageIsLastPage}
            onClick={() => onUpdatePage(totalPages)}
          >
            <span className={"visually-hidden"}>
              Go to last page, page {totalPages}
            </span>
            <Icon aria-hidden={true} icon={chevronForward} />
          </GhostButton>
        </li>
      </Flexbox>
    </nav>
  );
}

export { Pagination };
