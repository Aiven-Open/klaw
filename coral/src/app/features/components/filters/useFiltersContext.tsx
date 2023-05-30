import { ReactNode, createContext, useContext } from "react";
import { useSearchParams } from "react-router-dom";
import { AclType } from "src/domain/acl/acl-types";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";

type SetFiltersParams =
  | { name: "environment"; value: string }
  | { name: "aclType"; value: AclType | "ALL" }
  | { name: "status"; value: RequestStatus }
  | { name: "teamId"; value: string }
  | { name: "showOnlyMyRequests"; value: boolean }
  | { name: "requestType"; value: RequestOperationType | "ALL" }
  | { name: "search"; value: string };

type UseFiltersValuesParams =
  | {
      environment?: string;
      aclType?: AclType | "ALL";
      status?: RequestStatus;
      teamId?: string;
      showOnlyMyRequests?: boolean;
      requestType?: RequestOperationType | "ALL";
      search?: string;
      paginated?: boolean;
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
  setFilterValue: ({ name, value }: SetFiltersParams) => void;
};

const emptyValues: Omit<UseFilterValuesReturn, "setFilterValue"> = {
  environment: "ALL",
  aclType: "ALL",
  status: "ALL",
  teamId: "ALL",
  showOnlyMyRequests: false,
  requestType: "ALL",
  search: "",
};

const FiltersContext = createContext<UseFilterValuesReturn>({
  ...emptyValues,
  setFilterValue: () => null,
});

const useFiltersContext = () => useContext(FiltersContext);

const FiltersProvider = ({
  children,
  defaultValues = {},
}: {
  children: ReactNode;
  defaultValues: UseFiltersValuesParams;
}) => {
  const [searchParams, setSearchParams] = useSearchParams();

  //
  const initialvalues = { ...emptyValues, paginated: true, ...defaultValues };

  const environment =
    searchParams.get("environment") ?? initialvalues.environment;
  const aclType =
    (searchParams.get("aclType") as AclType | "ALL") ?? initialvalues.aclType;
  const status =
    (searchParams.get("status") as RequestStatus) ?? initialvalues.status;
  const teamId = searchParams.get("teamId") ?? initialvalues.teamId;
  const showOnlyMyRequests = searchParams.get("showOnlyMyRequests") === "true";
  const requestType =
    (searchParams.get("requestType") as RequestOperationType | "ALL") ??
    initialvalues.requestType;
  const search = searchParams.get("search") ?? initialvalues.search;
  const paginated = initialvalues.paginated;

  const setFilterValue = ({ name, value }: SetFiltersParams) => {
    const parsedValue = typeof value === "boolean" ? String(value) : value;
    searchParams.set(name, parsedValue);

    if (parsedValue === initialvalues[name]) {
      searchParams.delete(name);
    }

    if (paginated) {
      searchParams.set("page", "1");
    }

    setSearchParams(searchParams);
  };

  const filterValues = {
    environment,
    aclType,
    status,
    teamId,
    showOnlyMyRequests,
    requestType,
    search,
    setFilterValue,
  };

  return (
    <FiltersContext.Provider value={filterValues}>
      {children}
    </FiltersContext.Provider>
  );
};

const withFiltersContext = ({
  element,
  defaultValues,
}: {
  element: React.ReactNode;
  defaultValues?: UseFiltersValuesParams;
}) => {
  const WrappedElement = () => (
    <FiltersProvider defaultValues={defaultValues}>{element}</FiltersProvider>
  );
  return WrappedElement;
};

export { useFiltersContext, FiltersProvider, withFiltersContext };
