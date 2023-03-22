import { NativeSelect } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import {
  requestOperationTypeNameMap,
  operationTypeList,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { RequestOperationType } from "src/domain/requests/requests-types";

type operationTypeFilterProps = {
  defaultOperationType: RequestOperationType;
};

function OperationTypeFilter(props: operationTypeFilterProps) {
  const { defaultOperationType } = props;
  const [searchParams, setSearchParams] = useSearchParams();
  const operationType =
    (searchParams.get("operationType") as RequestOperationType | null) ??
    defaultOperationType;

  const handleChangeOperationType = (
    nextOperationType: RequestOperationType
  ) => {
    searchParams.set("operationType", nextOperationType);
    searchParams.set("page", "1");
    setSearchParams(searchParams);
  };

  return (
    <NativeSelect
      labelText={"Filter by operation type"}
      key={"filter-operationType"}
      defaultValue={operationType}
      onChange={(e) => {
        const operationType = e.target.value as RequestOperationType;
        return handleChangeOperationType(operationType);
      }}
    >
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

export default OperationTypeFilter;
