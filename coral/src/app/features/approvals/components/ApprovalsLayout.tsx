import { Alert, Box } from "@aivenio/aquarium";
import { ReactElement } from "react";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import { parseErrorMsg } from "src/services/mutation-utils";

type ApprovalsLayoutProps = {
  isLoading?: boolean;
  isErrorLoading?: boolean;
  errorMessage?: unknown;
  filters: ReactElement[];
  table: ReactElement;
  pagination?: ReactElement;
};

function ApprovalsLayout(props: ApprovalsLayoutProps) {
  const { filters, table, pagination, isLoading, isErrorLoading } = props;

  const errorMessage = parseErrorMsg(props.errorMessage);

  return (
    <>
      <Box
        display={"flex"}
        flexDirection={"row"}
        alignItems={"center"}
        justifyContent={"stretch"}
        colGap={"l1"}
        marginY={"l1"}
      >
        {filters.map((element) => {
          return (
            <Box grow={1} key={element.key}>
              {element}
            </Box>
          );
        })}
      </Box>
      {isLoading && <SkeletonTable />}
      {isErrorLoading && (
        <Alert type={"error"}>{errorMessage}. Please try again later!</Alert>
      )}

      <Box
        display={"flex"}
        flexDirection={"column"}
        alignItems={"center"}
        rowGap={"l4"}
        className={"a11y-enhancement-data-table"}
      >
        {!isLoading && !isErrorLoading && table}
        {pagination}
      </Box>
    </>
  );
}

export { ApprovalsLayout };
