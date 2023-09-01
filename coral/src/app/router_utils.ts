import isString from "lodash/isString";

enum Routes {
  TOPICS = "/topics",
  TOPIC_OVERVIEW = "/topic/:topicName",
  CONNECTORS = "/connectors",
  CONNECTOR_OVERVIEW = "/connector/:connectorName",
  CONNECTOR_REQUEST = "/connectors/request",
  CONNECTOR_EDIT_REQUEST = "/connector/:connectorName/request-update",
  TOPIC_REQUEST = "/topics/request",
  TOPIC_ACL_REQUEST = "/topic/:topicName/subscribe",
  TOPIC_SCHEMA_REQUEST = "/topic/:topicName/request-schema",
  TOPIC_PROMOTION_REQUEST = "/topic/:topicName/request-promotion",
  TOPIC_EDIT_REQUEST = "/topic/:topicName/request-update",
  ACL_REQUEST = "/request/acl",
  SCHEMA_REQUEST = "/request/schema",
  REQUESTS = "/requests",
  APPROVALS = "/approvals",
  CONFIGURATION = "/configuration",
  ENVIRONMENTS = "/configuration/environments",
}

enum EnvironmentsTabEnum {
  KAFKA = "ENVIRONMENTS_TAB_ENUM_kafka",
  SCHEMA_REGISTRY = "ENVIRONMENTS_TAB_ENUM_schema_registry",
  KAFKA_CONNECT = "ENVIRONMENTS_TAB_ENUM_kafka_connect",
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
  SETTINGS = "CONNECTOR_OVERVIEW_TAB_ENUM_settings",
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

const ENVIRONMENT_TAB_ID_INTO_PATH = {
  [EnvironmentsTabEnum.KAFKA]: "kafka",
  [EnvironmentsTabEnum.SCHEMA_REGISTRY]: "schema-registry",
  [EnvironmentsTabEnum.KAFKA_CONNECT]: "kafka-connect",
} as const;

const TOPIC_OVERVIEW_TAB_ID_INTO_PATH = {
  [TopicOverviewTabEnum.OVERVIEW]: "overview",
  [TopicOverviewTabEnum.ACLS]: "subscriptions",
  [TopicOverviewTabEnum.MESSAGES]: "messages",
  [TopicOverviewTabEnum.SCHEMA]: "schema",
  [TopicOverviewTabEnum.DOCUMENTATION]: "readme",
  [TopicOverviewTabEnum.HISTORY]: "history",
  [TopicOverviewTabEnum.SETTINGS]: "settings",
} as const;

const CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH = {
  [ConnectorOverviewTabEnum.OVERVIEW]: "overview",
  [ConnectorOverviewTabEnum.DOCUMENTATION]: "readme",
  [ConnectorOverviewTabEnum.HISTORY]: "history",
  [ConnectorOverviewTabEnum.SETTINGS]: "settings",
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

function isEnvironmentsTabEnum(value: unknown): value is EnvironmentsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      ENVIRONMENT_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}
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
  EnvironmentsTabEnum,
  RequestsTabEnum,
  ApprovalsTabEnum,
  TopicOverviewTabEnum,
  ConnectorOverviewTabEnum,
  Routes,
  ENVIRONMENT_TAB_ID_INTO_PATH,
  REQUESTS_TAB_ID_INTO_PATH,
  APPROVALS_TAB_ID_INTO_PATH,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  isEnvironmentsTabEnum,
  isRequestsTabEnum,
  isApprovalsTabEnum,
  isTopicsOverviewTabEnum,
  isConnectorsOverviewTabEnum,
};
