import { Box } from "@aivenio/aquarium";
import { Pagination } from "src/app/components/Pagination";
import { ReactElement } from "react";

type ApprovalsWrapperProps = {
  filters: ReactElement[];
  // add <typeof TypeApprovalsComponentTable> later
  table: ReactElement;
  pagination?: ReactElement<typeof Pagination>;
};

function ApprovalsLayout(props: ApprovalsWrapperProps) {
  const { filters, table, pagination } = props;
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

      <Box
        display={"flex"}
        flexDirection={"column"}
        alignItems={"center"}
        rowGap={"l4"}
        className={"a11y-enhancement-data-table"}
      >
        {table}
        {pagination}
      </Box>
    </>
  );
}

export { ApprovalsLayout };
