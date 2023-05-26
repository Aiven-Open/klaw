import { NativeSelect } from "@aivenio/aquarium";
import {
  requestStatusNameMap,
  statusList,
} from "src/app/features/approvals/utils/request-status-helper";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersValues";
import { RequestStatus } from "src/domain/requests/requests-types";

function StatusFilter() {
  const { status, setFilterValue } = useFiltersContext();

  return (
    <NativeSelect
      labelText={"Filter by status"}
      key={"filter-status"}
      defaultValue={status}
      onChange={(e) => {
        const status = e.target.value as RequestStatus;
        return setFilterValue({ name: "status", value: status });
      }}
    >
      {statusList.map((status) => {
        return (
          <option key={status} value={status}>
            {requestStatusNameMap[status]}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export default StatusFilter;
