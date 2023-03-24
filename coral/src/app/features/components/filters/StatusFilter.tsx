import { NativeSelect } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import {
  requestStatusNameMap,
  statusList,
} from "src/app/features/approvals/utils/request-status-helper";
import { RequestStatus } from "src/domain/requests/requests-types";

type StatusFilterProps = {
  defaultStatus: RequestStatus;
};

function StatusFilter(props: StatusFilterProps) {
  const { defaultStatus } = props;
  const [searchParams, setSearchParams] = useSearchParams();
  const status =
    (searchParams.get("status") as RequestStatus | null) ?? defaultStatus;

  const handleChangeStatus = (nextStatus: RequestStatus) => {
    searchParams.set("status", nextStatus);
    searchParams.set("page", "1");
    setSearchParams(searchParams);
  };

  return (
    <NativeSelect
      labelText={"Filter by status"}
      key={"filter-status"}
      defaultValue={status}
      onChange={(e) => {
        const status = e.target.value as RequestStatus;
        return handleChangeStatus(status);
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
