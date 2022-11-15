import { Flexbox, Icon } from "@aivenio/design-system";
import chevronBackward from "@aivenio/design-system/dist/src/icons/chevronBackward";
import chevronLeft from "@aivenio/design-system/dist/src/icons/chevronLeft";
import chevronRight from "@aivenio/design-system/dist/src/icons/chevronRight";
import chevronForward from "@aivenio/design-system/dist/src/icons/chevronForward";
import classes from "src/app/components/Pagination.module.css";

type PaginationProps = {
  activePage: number;
  totalPages: number;
};

function Pagination(props: PaginationProps) {
  const { activePage, totalPages } = props;

  return (
    <nav role="navigation" aria-label="Pagination">
      <Flexbox htmlTag={"ul"} colGap={"l1"} alignItems={"center"}>
        <li>
          <span tabIndex={0} className={classes.visuallyHidden}>
            {`You are on page ${activePage} of ${totalPages}`}
          </span>
        </li>
        <li aria-hidden={activePage === 1}>
          <a href={""} tabIndex={0}>
            <span className={classes.visuallyHidden}>Go to first page</span>
            <Icon aria-hidden={true} icon={chevronBackward} />
          </a>
        </li>
        <li aria-hidden={activePage === 1}>
          <a href={""} tabIndex={0}>
            <span className={classes.visuallyHidden}>
              Go to previous page, page {activePage - 1}
            </span>
            <Icon aria-hidden={true} icon={chevronLeft} />
          </a>
        </li>
        <li>
          <span tabIndex={-1} aria-hidden={true} />
          Page {activePage} of {totalPages}
        </li>
        <li aria-hidden={activePage === totalPages}>
          <a href={""} tabIndex={0}>
            <span className={classes.visuallyHidden}>
              Go to next page, page {activePage + 1}
            </span>
            <Icon aria-hidden={true} icon={chevronRight} />
          </a>
        </li>
        <li aria-hidden={activePage === totalPages}>
          <a href={""} tabIndex={0}>
            <span className={classes.visuallyHidden}>
              Go to last page, page {totalPages}
            </span>
            <Icon aria-hidden={true} icon={chevronForward} />
          </a>
        </li>
      </Flexbox>
    </nav>
  );
}

export { Pagination };
