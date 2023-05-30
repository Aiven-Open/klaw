import { NativeSelect } from "@aivenio/aquarium";
import { ChangeEvent } from "react";
import {
  requestOperationTypeList,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { RequestOperationType } from "src/domain/requests/requests-types";
import { ResolveIntersectionTypes } from "types/utils";

type RequestOperationTypeOptions = ResolveIntersectionTypes<
  RequestOperationType | "ALL"
>;

function RequestTypeFilter() {
  const { requestType, setFilterValue } = useFiltersContext();

  const handleChangeRequestType = (e: ChangeEvent<HTMLSelectElement>) => {
    const nextOperationType = e.target.value as RequestOperationTypeOptions;

    setFilterValue({ name: "requestType", value: nextOperationType });
  };

  return (
    <NativeSelect
      labelText={"Filter by request type"}
      key={"filter-request-type"}
      defaultValue={requestType}
      onChange={handleChangeRequestType}
    >
      <option key={"ALL"} value={"ALL"}>
        All request types
      </option>
      {requestOperationTypeList.map((operationType) => {
        return (
          <option key={operationType} value={operationType}>
            {requestOperationTypeNameMap[operationType]}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { RequestTypeFilter };
