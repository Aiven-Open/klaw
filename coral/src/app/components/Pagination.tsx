import { Box, Button, Icon } from "@aivenio/aquarium";
import chevronBackward from "@aivenio/aquarium/dist/src/icons/chevronBackward";
import chevronLeft from "@aivenio/aquarium/dist/src/icons/chevronLeft";
import chevronRight from "@aivenio/aquarium/dist/src/icons/chevronRight";
import chevronForward from "@aivenio/aquarium/dist/src/icons/chevronForward";

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
      <Box
        component={"ul"}
        display={"flex"}
        colGap={"l1"}
        alignItems={"center"}
      >
        <li aria-hidden={currentPageIsFirstPage}>
          <Button.Ghost
            disabled={currentPageIsFirstPage}
            onClick={() => onUpdatePage(1)}
          >
            <span className={"visually-hidden"}>Go to first page</span>
            <Icon aria-hidden={true} icon={chevronBackward} />
          </Button.Ghost>
        </li>
        <li aria-hidden={currentPageIsFirstPage}>
          <Button.Ghost
            disabled={currentPageIsFirstPage}
            onClick={() => onUpdatePage(activePage - 1)}
          >
            <span className={"visually-hidden"}>
              Go to previous page, page {activePage - 1}
            </span>
            <Icon aria-hidden={true} icon={chevronLeft} />
          </Button.Ghost>
        </li>
        <li aria-hidden={true}>
          Page {activePage} of {totalPages}
        </li>
        <li aria-hidden={currentPageIsLastPage}>
          <Button.Ghost
            disabled={currentPageIsLastPage}
            onClick={() => onUpdatePage(activePage + 1)}
          >
            <span className={"visually-hidden"}>
              Go to next page, page {activePage + 1}
            </span>
            <Icon aria-hidden={true} icon={chevronRight} />
          </Button.Ghost>
        </li>
        <li aria-hidden={currentPageIsLastPage}>
          <Button.Ghost
            disabled={currentPageIsLastPage}
            onClick={() => onUpdatePage(totalPages)}
          >
            <span className={"visually-hidden"}>
              Go to last page, page {totalPages}
            </span>
            <Icon aria-hidden={true} icon={chevronForward} />
          </Button.Ghost>
        </li>
      </Box>
    </nav>
  );
}

export { Pagination };
