import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import SelectTeam from "src/app/features/components/table-filters/SelectTeam";
import SelectEnvironment from "src/app/features/topics/browse/components/select-environment/SelectEnvironment";
import { RequestStatus } from "src/domain/requests";
import { TopicRequest } from "src/domain/topic/topic-types";

const statusList: RequestStatus[] = [
  "ALL",
  "CREATED",
  "APPROVED",
  "DECLINED",
  "DELETED",
];

const useTableFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const envParam = searchParams.get("env");
  const statusParam = searchParams.get("status") as RequestStatus | null;
  const teamParam = searchParams.get("team");

  const [environment, setEnvironment] = useState(envParam ?? "ALL");
  const [status, setStatus] = useState<RequestStatus>(statusParam ?? "CREATED");
  const [team, setTeam] = useState<TopicRequest["teamname"]>(
    teamParam ?? "ALL"
  );
  const [topic, setTopic] = useState(searchParams.get("topic") ?? "");

  const filters = [
    <SelectEnvironment key={"environment"} onChange={setEnvironment} />,
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
        if (status === "ALL") {
          return (
            <option key={status} value={"ALL"}>
              All statuses
            </option>
          );
        }
        return <option key={status}>{status}</option>;
      })}
    </NativeSelect>,
    <SelectTeam key={"team"} onChange={setTeam} />,
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

  return { environment, status, team, topic, filters };
};

export default useTableFilters;
