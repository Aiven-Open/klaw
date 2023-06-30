import isString from "lodash/isString";

enum Routes {
  TOPICS = "/topics",
  TOPIC_OVERVIEW = "/topic/:topicName",
  CONNECTORS = "/connectors",
  CONNECTOR_OVERVIEW = "/connector/:connectorName",
  CONNECTOR_REQUEST = "/connectors/request",
  TOPIC_REQUEST = "/topics/request",
  TOPIC_ACL_REQUEST = "/topic/:topicName/subscribe",
  TOPIC_SCHEMA_REQUEST = "/topic/:topicName/request-schema",
  TOPIC_PROMOTION_REQUEST = "/topic/:topicName/request-promotion",
  ACL_REQUEST = "/request/acl",
  SCHEMA_REQUEST = "/request/schema",
  REQUESTS = "/requests",
  APPROVALS = "/approvals",
}

enum TopicOverviewTabEnum {
  OVERVIEW = "TOPIC_OVERVIEW_TAB_ENUM_overview",
  ACLS = "TOPIC_OVERVIEW_TAB_ENUM_acls",
  MESSAGES = "TOPIC_OVERVIEW_TAB_ENUM_messages",
  SCHEMA = "TOPIC_OVERVIEW_TAB_ENUM_schemas",
  DOCUMENTATION = "TOPIC_OVERVIEW_TAB_ENUM_documentation",
  HISTORY = "TOPIC_OVERVIEW_TAB_ENUM_history",
  SETTINGS = "TOPIC_OVERVIEW_TAB_ENUM_settings",
}

enum ConnectorOverviewTabEnum {
  OVERVIEW = "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
  DOCUMENTATION = "CONNECTOR_OVERVIEW_TAB_ENUM_documentation",
  HISTORY = "CONNECTOR_OVERVIEW_TAB_ENUM_history",
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

const TOPIC_OVERVIEW_TAB_ID_INTO_PATH = {
  [TopicOverviewTabEnum.OVERVIEW]: "overview",
  [TopicOverviewTabEnum.ACLS]: "subscriptions",
  [TopicOverviewTabEnum.MESSAGES]: "messages",
  [TopicOverviewTabEnum.SCHEMA]: "schema",
  [TopicOverviewTabEnum.DOCUMENTATION]: "documentation",
  [TopicOverviewTabEnum.HISTORY]: "history",
  [TopicOverviewTabEnum.SETTINGS]: "settings",
} as const;

const CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH = {
  [ConnectorOverviewTabEnum.OVERVIEW]: "overview",
  [ConnectorOverviewTabEnum.DOCUMENTATION]: "documentation",
  [ConnectorOverviewTabEnum.HISTORY]: "history",
} as const;

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

function isTopicsOverviewTabEnum(
  value: unknown
): value is TopicOverviewTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

function isConnectorsOverviewTabEnum(
  value: unknown
): value is ConnectorOverviewTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

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
  TopicOverviewTabEnum,
  ConnectorOverviewTabEnum,
  Routes,
  REQUESTS_TAB_ID_INTO_PATH,
  APPROVALS_TAB_ID_INTO_PATH,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  isRequestsTabEnum,
  isApprovalsTabEnum,
  isTopicsOverviewTabEnum,
  isConnectorsOverviewTabEnum,
};
