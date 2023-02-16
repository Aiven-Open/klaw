import { Box } from "@aivenio/aquarium";
import { ReactElement } from "react";

type ApprovalsLayoutProps = {
  filters: ReactElement[];
  table: ReactElement;
  pagination?: ReactElement;
};

function ApprovalsLayout(props: ApprovalsLayoutProps) {
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
