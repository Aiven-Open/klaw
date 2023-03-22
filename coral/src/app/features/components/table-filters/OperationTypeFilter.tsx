import { NativeSelect } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import {
  requestOperationTypeNameMap,
  operationTypeList,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { RequestOperationType } from "src/domain/requests/requests-types";
import { ResolveIntersectionTypes } from "types/utils";

type RequestOperationTypeOptions = ResolveIntersectionTypes<
  RequestOperationType | "ALL"
>;

function OperationTypeFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const operationType =
    (searchParams.get("operationType") as RequestOperationTypeOptions | null) ??
    "ALL";

  const handleChangeOperationType = (
    nextOperationType: RequestOperationTypeOptions
  ) => {
    if (nextOperationType === "ALL") {
      searchParams.delete("operationType");
      searchParams.set("page", "1");
    } else {
      searchParams.set("operationType", nextOperationType);
      searchParams.set("page", "1");
    }

    setSearchParams(searchParams);
  };

  return (
    <NativeSelect
      labelText={"Filter by operation type"}
      key={"filter-operationType"}
      defaultValue={operationType}
      onChange={(e) => {
        const operationType = e.target.value as RequestOperationTypeOptions;
        return handleChangeOperationType(operationType);
      }}
    >
      <option key={"ALL"} value={"ALL"}>
        All operation types
      </option>
      {operationTypeList.map((operationType) => {
        return (
          <option key={operationType} value={operationType}>
            {requestOperationTypeNameMap[operationType]}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { OperationTypeFilter };
