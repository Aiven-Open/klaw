import isString from "lodash/isString";

enum Routes {
  TOPICS = "/topics",
  TOPIC_REQUEST = "/topics/request",
  TOPIC_ACL_REQUEST = "/topic/:topicName/subscribe",
  TOPIC_SCHEMA_REQUEST = "/topic/:topicName/request-schema",
  REQUESTS = "/requests",
  APPROVALS = "/approvals",
}

enum RequestsTabEnum {
  TOPICS = "REQUESTS_TAB_ENUM_topics",
  ACLS = "REQUESTS_TAB_ENUM_acls",
  SCHEMAS = "REQUESTS_TAB_ENUM_schemas",
  CONNECTORS = "REQUESTS_TAB_ENUM_connectors",
}

enum ApprovalsTabEnum {
  TOPICS = "APPROVALS_TAB_ENUM_topics",
  ACLS = "APPROVALS_TAB_ENUM_acls",
  SCHEMAS = "APPROVALS_TAB_ENUM_schemas",
  CONNECTORS = "APPROVALS_TAB_ENUM_connectors",
}

const REQUESTS_TAB_ID_INTO_PATH = {
  [RequestsTabEnum.TOPICS]: "topics",
  [RequestsTabEnum.ACLS]: "acls",
  [RequestsTabEnum.SCHEMAS]: "schemas",
  [RequestsTabEnum.CONNECTORS]: "connectors",
} as const;

const APPROVALS_TAB_ID_INTO_PATH = {
  [ApprovalsTabEnum.TOPICS]: "topics",
  [ApprovalsTabEnum.ACLS]: "acls",
  [ApprovalsTabEnum.SCHEMAS]: "schemas",
  [ApprovalsTabEnum.CONNECTORS]: "connectors",
} as const;

function isRequestsTabEnum(value: unknown): value is RequestsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      REQUESTS_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

function isApprovalsTabEnum(value: unknown): value is ApprovalsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      APPROVALS_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

export {
  RequestsTabEnum,
  ApprovalsTabEnum,
  Routes,
  REQUESTS_TAB_ID_INTO_PATH,
  APPROVALS_TAB_ID_INTO_PATH,
  isRequestsTabEnum,
  isApprovalsTabEnum,
};
