import { useSearchParams } from "react-router-dom";
import { AclType } from "src/domain/acl/acl-types";
import {
  RequestStatus,
  RequestOperationType,
} from "src/domain/requests/requests-types";

type UseFiltersValuesParams =
  | {
      defaultTopic?: string;
      defaultEnvironment?: string;
      defaultAclType?: AclType | "ALL";
      defaultStatus?: RequestStatus;
      defaultTeam?: string;
      defaultOperationType?: RequestOperationType | "ALL";
      defaultShowOnlyMyRequests?: boolean;
    }
  | undefined;

const useFiltersValues = (defaultValues: UseFiltersValuesParams = {}) => {
  const [searchParams] = useSearchParams();
  const {
    defaultTopic = "",
    defaultEnvironment = "ALL",
    defaultAclType = "ALL",
    defaultStatus = "ALL",
    defaultTeam = "ALL",
    defaultOperationType = "ALL",
    defaultShowOnlyMyRequests = false,
  } = defaultValues;

  const topic = searchParams.get("topic") ?? defaultTopic;
  const environment = searchParams.get("environment") ?? defaultEnvironment;
  const aclType =
    (searchParams.get("aclType") as AclType | "ALL") ?? defaultAclType;
  const status = (searchParams.get("status") as RequestStatus) ?? defaultStatus;
  const team = searchParams.get("team") ?? defaultTeam;
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true"
      ? true
      : defaultShowOnlyMyRequests;
  const operationType =
    (searchParams.get("operationType") as RequestOperationType | "ALL") ??
    defaultOperationType;

  return {
    topic,
    environment,
    aclType,
    status,
    team,
    showOnlyMyRequests,
    operationType,
  };
};

export { useFiltersValues };
