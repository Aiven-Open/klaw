import { Alert, Box } from "@aivenio/aquarium";
import { ReactElement } from "react";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import { parseErrorMsg } from "src/services/mutation-utils";

type TableLayoutProps = {
  isLoading?: boolean;
  isErrorLoading?: boolean;
  errorMessage?: unknown;
  filters: ReactElement[];
  table: ReactElement;
  pagination?: ReactElement;
};

function TableLayout(props: TableLayoutProps) {
  const {
    filters,
    table,
    pagination,
    isLoading,
    isErrorLoading,
    errorMessage,
  } = props;

  return (
    <>
      <Box
        display={"flex"}
        flexDirection={"row"}
        flexWrap={"wrap"}
        alignItems={"center"}
        justifyContent={"stretch"}
        colGap={"l1"}
        marginY={"l1"}
      >
        {filters.map((element) => {
          return (
            <Box
              key={element.key}
              grow={1}
              display={"flex"}
              flexDirection={"column"}
            >
              {element}
            </Box>
          );
        })}
      </Box>
      {isLoading && <SkeletonTable />}
      {isErrorLoading && (
        <div role={"alert"}>
          <Alert type={"error"}>
            {parseErrorMsg(errorMessage)}. Please try again later!
          </Alert>
        </div>
      )}
      {!isLoading && !isErrorLoading && (
        <>
          <Box
            style={{
              overflow: "auto",
            }}
            marginBottom={"l4"}
          >
            <Box className={"a11y-enhancement-data-table"}>{table}</Box>
          </Box>
          <Box justifyContent={"center"}>{pagination}</Box>
        </>
      )}
    </>
  );
}

export { TableLayout };
