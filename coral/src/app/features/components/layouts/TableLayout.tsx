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
      <Box.Flex
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
            <Box.Flex
              key={element.key}
              grow={1}
              display={"flex"}
              flexDirection={"column"}
            >
              {element}
            </Box.Flex>
          );
        })}
      </Box.Flex>
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
          <Box.Flex
            style={{
              overflow: "auto",
            }}
            marginBottom={"l4"}
          >
            <Box.Flex className={"a11y-enhancement-data-table"}>{table}</Box.Flex>
          </Box.Flex>
          <Box.Flex justifyContent={"center"}>{pagination}</Box.Flex>
        </>
      )}
    </>
  );
}

export { TableLayout };
