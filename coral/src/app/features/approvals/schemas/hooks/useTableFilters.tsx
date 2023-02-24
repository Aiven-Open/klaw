import { SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { RequestStatus } from "src/domain/requests";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import { ComplexNativeSelect } from "src/app/components/ComplexNativeSelect";
import { SelectSchemaRegEnvironment } from "src/app/features/approvals/schemas/components/SelectSchemaRegEnvironment";
import { ALL_ENVIRONMENTS_VALUE } from "src/domain/environment";

const statusList: RequestStatus[] = [
  "ALL",
  "CREATED",
  "APPROVED",
  "DECLINED",
  "DELETED",
];

const useTableFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const envParam = searchParams.get("environment");
  const statusParam = searchParams.get("status") as RequestStatus | null;
  const topicParam = searchParams.get("topic") as string | null;

  const [environment, setEnvironment] = useState(
    envParam ?? ALL_ENVIRONMENTS_VALUE
  );
  const [status, setStatus] = useState<RequestStatus>(statusParam ?? "CREATED");
  const [topic, setTopic] = useState(topicParam ?? "");

  const statusOptions: Array<{ value: RequestStatus; name: string }> =
    statusList.map((status) => {
      return { value: status, name: requestStatusNameMap[status] };
    });

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
    <ComplexNativeSelect<{ value: RequestStatus; name: string }>
      labelText={"Filter by status"}
      key={"filter-status"}
      defaultValue={status}
      options={statusOptions}
      identifierValue={"value"}
      identifierName={"name"}
      onBlur={(option) => {
        const { value } = option as { value: RequestStatus; name: string };
        searchParams.set("status", value);
        setSearchParams(searchParams);
        setStatus(value);
      }}
    />,
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
        Press &quot;Enter&quot; to start your search. Press &quot;Escape&quot;
        to delete all your input.
      </div>
    </div>,
  ];

  return { environment, topic, status, filters };
};

export default useTableFilters;
