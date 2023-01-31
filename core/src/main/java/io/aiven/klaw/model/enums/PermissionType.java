package io.aiven.klaw.model.enums;

public enum PermissionType {
  REQUEST_CREATE_TOPICS("To request for Topics"),

  REQUEST_EDIT_TOPICS("To request for Editing Topics"),
  REQUEST_DELETE_TOPICS("To request for deletion of topics"),
  REQUEST_CREATE_SUBSCRIPTIONS("To request for Producer or Consumer subscriptions"),
  REQUEST_DELETE_SUBSCRIPTIONS("To request for deletion of subscriptions"),
  REQUEST_CREATE_SCHEMAS("To request for Schemas"),
  REQUEST_DELETE_SCHEMAS("To request for deletion of schemas"),
  REQUEST_CREATE_CONNECTORS("To request for Kafka Connectors"),
  REQUEST_DELETE_CONNECTORS("To request for deletion of Kafka connectors"),

  APPROVE_TOPICS("To approve topics requests"),
  APPROVE_SUBSCRIPTIONS("To approve producer or consumer subscriptions"),
  APPROVE_SCHEMAS("To approve schemas"),
  APPROVE_CONNECTORS("To approve kafka connectors"),
  APPROVE_ALL_REQUESTS_TEAMS("To approve any requests of all teams within same tenant"),

  SYNC_TOPICS("To Synchronize topics From Cluster"),
  SYNC_SUBSCRIPTIONS("To Synchronize acls From Cluster"),
  SYNC_BACK_TOPICS("To Synchronize topics back to cluster"),
  SYNC_BACK_SUBSCRIPTIONS("To Synchronize subscriptions back to cluster"),
  SYNC_CONNECTORS("To Synchronize Kafka connectors From Cluster"),

  ADD_EDIT_DELETE_TEAMS("To add modify delete teams"),
  ADD_EDIT_DELETE_USERS("To add modify delete users"),
  ADD_EDIT_DELETE_CLUSTERS("To add modify delete clusters"),
  ADD_EDIT_DELETE_ENVS("To add modify delete environments"),
  ADD_EDIT_DELETE_ROLES("To add modify delete roles"),

  UPDATE_PERMISSIONS("To update permissions"),
  VIEW_TOPICS("View topics"),
  VIEW_CONNECTORS("View kafka connectors"),

  ALL_TEAMS_REPORTS("To view and download reports of all teams"),
  UPDATE_DELETE_MY_TENANT("To update or delete my tenant"),
  UPDATE_SERVERCONFIG("To update server configuration and properties"),
  ADD_TENANT("To add tenant"),
  //    ALL_TENANTS_REPORTS("To view and download reports of all tenants and teams"),
  FULL_ACCESS_USERS_TEAMS_ROLES("To assign any role to any user., view all teams of tenant."),
  SHUTDOWN_KLAW("To shutdown Klaw");

  String description;

  PermissionType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }
}
