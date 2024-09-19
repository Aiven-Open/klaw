import { ReactNode, createContext, useContext } from "react";
import { useSearchParams } from "react-router-dom";
import { AclType } from "src/domain/acl/acl-types";
import { ClusterType } from "src/domain/cluster";
import { RequestOperationType, RequestStatus } from "src/domain/requests";
import { TopicType } from "src/domain/topic";

type SetFiltersParams =
  | { name: "environment"; value: string }
  | { name: "aclType"; value: AclType | "ALL" }
  | { name: "status"; value: RequestStatus }
  | { name: "teamId"; value: string }
  | { name: "showOnlyMyRequests"; value: boolean }
  | { name: "requestType"; value: RequestOperationType }
  | { name: "search"; value: string }
  | { name: "teamName"; value: string }
  | { name: "topicType"; value: TopicType | "ALL" }
  | { name: "clusterType"; value: ClusterType };

interface UseFiltersDefaultValues {
  environment: string;
  aclType: AclType | "ALL";
  status: RequestStatus;
  teamId: string;
  showOnlyMyRequests: boolean;
  requestType: RequestOperationType;
  search: string;
  paginated: boolean;
  teamName: string;
  topicType: TopicType | "ALL";
  clusterType: ClusterType;
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
  teamName: "ALL",
  showOnlyMyRequests: false,
  requestType: "ALL",
  search: "",
  paginated: true,
  topicType: "ALL",
  clusterType: "ALL",
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
    (searchParams.get("requestType") as RequestOperationType) ??
    initialValues.requestType;
  const search = searchParams.get("search") ?? initialValues.search;
  const paginated = initialValues.paginated;
  const teamName = searchParams.get("teamName") ?? initialValues.teamName;
  const topicType =
    (searchParams.get("topicType") as TopicType) ?? initialValues.topicType;
  const clusterType =
    (searchParams.get("clusterType") as ClusterType) ??
    initialValues.clusterType;

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
    teamName,
    showOnlyMyRequests,
    requestType,
    search,
    topicType,
    clusterType,
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

export { FiltersProvider, useFiltersContext, withFiltersContext };
