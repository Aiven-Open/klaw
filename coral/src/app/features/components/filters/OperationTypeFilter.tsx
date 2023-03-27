import { NativeSelect } from "@aivenio/aquarium";
import { ChangeEvent } from "react";
import {
  operationTypeList,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { RequestOperationType } from "src/domain/requests/requests-types";
import { ResolveIntersectionTypes } from "types/utils";

type RequestOperationTypeOptions = ResolveIntersectionTypes<
  RequestOperationType | "ALL"
>;

function OperationTypeFilter() {
  const { operationType, setFilterValue } = useFiltersValues();

  const handleChangeOperationType = (e: ChangeEvent<HTMLSelectElement>) => {
    const nextOperationType = e.target.value as RequestOperationTypeOptions;

    setFilterValue({ name: "operationType", value: nextOperationType });
  };

  return (
    <NativeSelect
      labelText={"Filter by operation type"}
      key={"filter-operationType"}
      defaultValue={operationType}
      onChange={handleChangeOperationType}
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
