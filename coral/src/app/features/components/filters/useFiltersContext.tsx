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

interface UseFiltersDefaultValues {
  environment: string;
  aclType: AclType | "ALL";
  status: RequestStatus;
  teamId: string;
  showOnlyMyRequests: boolean;
  requestType: RequestOperationType | "ALL";
  search: string;
  paginated: boolean;
}

interface UseFiltersReturnedValues
  extends Omit<UseFiltersDefaultValues, "paginated"> {
  setFilterValue: ({ name, value }: SetFiltersParams) => void;
}

const emptyValues: UseFiltersDefaultValues = {
  environment: "ALL",
  aclType: "ALL",
  status: "ALL",
  teamId: "ALL",
  showOnlyMyRequests: false,
  requestType: "ALL",
  search: "",
  paginated: true,
};

const FiltersContext = createContext<UseFiltersReturnedValues>({
  ...emptyValues,
  setFilterValue: () => null,
});

const useFiltersContext = () => useContext(FiltersContext);

const FiltersProvider = ({
  children,
  defaultValues = emptyValues,
}: {
  children: ReactNode;
  defaultValues?: Partial<UseFiltersDefaultValues>;
}) => {
  const [searchParams, setSearchParams] = useSearchParams();

  //
  const initialValues = { ...emptyValues, ...defaultValues };

  const environment =
    searchParams.get("environment") ?? initialValues.environment;
  const aclType =
    (searchParams.get("aclType") as AclType | "ALL") ?? initialValues.aclType;
  const status =
    (searchParams.get("status") as RequestStatus) ?? initialValues.status;
  const teamId = searchParams.get("teamId") ?? initialValues.teamId;
  const showOnlyMyRequests = searchParams.get("showOnlyMyRequests") === "true";
  const requestType =
    (searchParams.get("requestType") as RequestOperationType | "ALL") ??
    initialValues.requestType;
  const search = searchParams.get("search") ?? initialValues.search;
  const paginated = initialValues.paginated;

  const setFilterValue = ({ name, value }: SetFiltersParams) => {
    const parsedValue = typeof value === "boolean" ? String(value) : value;
    searchParams.set(name, parsedValue);

    if (value === initialValues[name]) {
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
  defaultValues?: Partial<UseFiltersDefaultValues>;
}) => {
  const WrappedElement = () => (
    <FiltersProvider defaultValues={defaultValues}>{element}</FiltersProvider>
  );
  return WrappedElement;
};

export { useFiltersContext, FiltersProvider, withFiltersContext };
