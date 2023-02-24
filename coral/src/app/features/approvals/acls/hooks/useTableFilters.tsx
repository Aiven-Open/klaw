import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import SelectEnvironment from "src/app/features/topics/browse/components/select-environment/SelectEnvironment";
import { RequestStatus } from "src/domain/requests";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";

const statusList: RequestStatus[] = [
  "ALL",
  "CREATED",
  "APPROVED",
  "DECLINED",
  "DELETED",
];
type AclType = "ALL" | "CONSUMER" | "PRODUCER";
const aclTypes: AclType[] = ["ALL", "CONSUMER", "PRODUCER"];

const useTableFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const envParam = searchParams.get("env");
  const statusParam = searchParams.get("status") as RequestStatus | null;
  const aclTypeParam = searchParams.get("aclType") as AclType | null;

  const [environment, setEnvironment] = useState(envParam ?? "ALL");
  const [status, setStatus] = useState<RequestStatus>(statusParam ?? "CREATED");
  const [aclType, setAclType] = useState<AclType>(aclTypeParam ?? "ALL");
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
        return (
          <option key={status} value={status}>
            {requestStatusNameMap[status]}
          </option>
        );
      })}
    </NativeSelect>,
    <NativeSelect
      labelText={"Filter by ACL type"}
      key={"filter-acl-type"}
      defaultValue={aclType}
      onChange={(e) => {
        const selectedType = e.target.value as AclType;
        searchParams.set("aclType", selectedType);
        setSearchParams(searchParams);
        setAclType(selectedType);
      }}
    >
      {aclTypes.map((type) => {
        if (type === "ALL") {
          return (
            <option key={type} value="ALL">
              All ACL types
            </option>
          );
        }
        return <option key={type}>{type}</option>;
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
        Press &quot;Enter&quot; to start your search. Press &quot;Escape&quot;
        to delete all your input.
      </div>
    </div>,
  ];

  return { environment, status, aclType, topic, filters };
};

export default useTableFilters;
