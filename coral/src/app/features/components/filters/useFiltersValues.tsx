import { useSearchParams } from "react-router-dom";
import { AclType } from "src/domain/acl/acl-types";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";

type SetFiltersParams =
  | { name: "environment"; value: string; paginated?: boolean }
  | { name: "aclType"; value: AclType | "ALL"; paginated?: boolean }
  | { name: "status"; value: RequestStatus; paginated?: boolean }
  | { name: "teamId"; value: string; paginated?: boolean }
  | { name: "showOnlyMyRequests"; value: boolean; paginated?: boolean }
  | {
      name: "requestType";
      value: RequestOperationType | "ALL";
      paginated?: boolean;
    }
  | { name: "search"; value: string; paginated?: boolean };

type UseFiltersValuesParams =
  | {
      defaultEnvironment?: string;
      defaultAclType?: AclType | "ALL";
      defaultStatus?: RequestStatus;
      defaultTeam?: string;
      defaultRequestType?: RequestOperationType | "ALL";
      defaultShowOnlyMyRequests?: boolean;
      defaultSearch?: string;
    }
  | undefined;

type UseFilterValuesReturn = {
  environment: string;
  aclType: AclType | "ALL";
  status: RequestStatus;
  teamId: string;
  showOnlyMyRequests: boolean;
  requestType: RequestOperationType | "ALL";
  search: string;
  setFilterValue: ({ name, value, paginated }: SetFiltersParams) => void;
};

const useFiltersValues = (
  defaultValues: UseFiltersValuesParams = {}
): UseFilterValuesReturn => {
  const [searchParams, setSearchParams] = useSearchParams();
  const {
    defaultEnvironment = "ALL",
    defaultAclType = "ALL",
    defaultStatus = "ALL",
    defaultTeam = "ALL",
    defaultRequestType = "ALL",
    defaultShowOnlyMyRequests = false,
    defaultSearch = "",
  } = defaultValues;

  const environment = searchParams.get("environment") ?? defaultEnvironment;
  const aclType =
    (searchParams.get("aclType") as AclType | "ALL") ?? defaultAclType;
  const status = (searchParams.get("status") as RequestStatus) ?? defaultStatus;
  const teamId = searchParams.get("teamId") ?? defaultTeam;
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true"
      ? true
      : defaultShowOnlyMyRequests;
  const requestType =
    (searchParams.get("requestType") as RequestOperationType | "ALL") ??
    defaultRequestType;
  const search = searchParams.get("search") ?? defaultSearch;

  const setFilterValue = ({
    name,
    value,
    paginated = true,
  }: SetFiltersParams) => {
    const parsedValue = typeof value === "boolean" ? String(value) : value;
    searchParams.set(name, parsedValue);

    if (
      (value === "ALL" && name !== "status") ||
      value === "" ||
      value === false
    ) {
      searchParams.delete(name);
    }

    if (paginated) {
      searchParams.set("page", "1");
    }

    setSearchParams(searchParams);
  };

  return {
    environment,
    aclType,
    status,
    teamId,
    showOnlyMyRequests,
    requestType,
    search,
    setFilterValue,
  };
};

export { useFiltersValues };
