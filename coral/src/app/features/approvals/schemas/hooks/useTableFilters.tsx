import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { RequestStatus } from "src/domain/requests/requests-types";
import {
  requestStatusNameMap,
  statusList,
} from "src/app/features/approvals/utils/request-status-helper";
import { SelectSchemaRegEnvironment } from "src/app/features/approvals/schemas/components/SelectSchemaRegEnvironment";
import { ALL_ENVIRONMENTS_VALUE } from "src/domain/environment";

const useTableFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const envParam = searchParams.get("environment");
  const statusParam = searchParams.get("status") as RequestStatus | null;
  const topicParam = searchParams.get("topic");

  const [environment, setEnvironment] = useState(
    envParam ?? ALL_ENVIRONMENTS_VALUE
  );
  const [status, setStatus] = useState<RequestStatus>(statusParam ?? "CREATED");
  const [topic, setTopic] = useState(topicParam ?? "");

  const filters = [
    <SelectSchemaRegEnvironment
      key={"filter-environment"}
      value={environment}
      onChange={(envId: string) => {
        searchParams.set("environment", envId);
        setSearchParams(searchParams);
        setEnvironment(envId);
      }}
    />,
    <NativeSelect
      labelText={"Filter by status"}
      key={"filter-status"}
      defaultValue={status}
      onChange={(e) => {
        const status = e.target.value as RequestStatus;
        searchParams.set("status", status);
        setSearchParams(searchParams);
        setStatus(status);
      }}
    >
      {statusList.map((status) => {
        return (
          <option key={status} value={status}>
            {requestStatusNameMap[status]}
          </option>
        );
      })}
    </NativeSelect>,

    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={"Search Topic (exact match)"}
        defaultValue={topic}
        onChange={debounce((e) => {
          const parsedName = String(e.target.value).trim();
          if (parsedName === "") {
            searchParams.delete("topic");
            setTopic(parsedName);
            setSearchParams(searchParams);
          } else {
            searchParams.set("topic", parsedName);
            setSearchParams(searchParams);
            setTopic(parsedName);
          }
        }, 500)}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Search for an exact match for topic name. Searching starts automatically
        with a little delay while typing. Press &quot;Escape&quot; to delete all
        your input.
      </div>
    </div>,
  ];

  return { environment, topic, status, filters };
};

export default useTableFilters;
