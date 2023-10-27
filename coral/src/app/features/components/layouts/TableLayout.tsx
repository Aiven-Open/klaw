import { Alert, Box } from "@aivenio/aquarium";
import { ReactElement } from "react";
import SkeletonTable from "src/app/features/approvals/SkeletonTable";
import { parseErrorMsg } from "src/services/mutation-utils";
import {LoadingTableProps, LoadingTable} from "./LoadingTable"
type TableLayoutProps = {
  isLoading?: boolean;
  isErrorLoading?: boolean;
  errorMessage?: unknown;
  filters: ReactElement[];
  table: ReactElement;
  pagination?: ReactElement;
  loadingState?: LoadingTableProps;
};

function TableLayout(props: TableLayoutProps) {
  const {
    filters,
    table,
    pagination,
    isLoading,
    isErrorLoading,
    errorMessage,
    loadingState,
  } = props;

  return (
    <>
      <Box.Flex
        flexDirection={"row"}
        flexWrap={"wrap"}
        alignItems={"center"}
        justifyContent={"stretch"}
        colGap={"l1"}
        marginY={"l1"}
      >
        {filters.map((element) => {
          return (
            <Box.Flex key={element.key} flexDirection={"column"}>
              {element}
            </Box.Flex>
          );
        })}
      </Box.Flex>

      {loadingState ? ( 
        <LoadingTable {...loadingState} />
      ) : isLoading ? (
        <SkeletonTable />
      ) : isErrorLoading ? (
        <Alert type={"error"}>
          {parseErrorMsg(errorMessage)}. Please try again later!
        </Alert>
      ) : (
        <>
          <Box
            style={{
              overflow: "auto",
            }}
            marginBottom={"l4"}
          >
            <Box className={"a11y-enhancement-data-table"}>{table}</Box>
          </Box>
          <Box.Flex justifyContent={"center"}>{pagination}</Box.Flex>
        </>
      )}
    </>
  );
}

export { TableLayout };
