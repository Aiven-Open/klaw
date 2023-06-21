package io.aiven.klaw.error;

public class KlawErrorMessages {

  public static final String ACTIVE_DIRECTORY_ERR_CODE_101 = "AD101";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_102 = "AD102";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_103 = "AD103";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_104 = "AD104";

  public static final String AD_ERROR_101_NO_MATCHING_ROLE =
      "No matching role is configured. Please make sure only one matching role"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_102_NO_MATCHING_TEAM =
      "No matching team is configured. Please make sure only one matching team"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_103_MULTIPLE_MATCHING_ROLE =
      "Multiple matching roles are configured. Please make sure only one matching role"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_104_MULTIPLE_MATCHING_TEAM =
      "Multiple matching teams are configured. Please make sure only one matching team"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String REQ_FAILURE =
      "Unable to process the request. Please contact our Administrator !!";

  public static final String ACL_ERR_101 = "Failure : Topic not found on target environment.";

  public static final String ACL_ERR_102 =
      "Failure : Please change the pattern to LITERAL for topic type.";

  // Generic err messages
  public static final String REQ_ERR_101 = "This request does not exist anymore.";
  public static final String SYNC_ERR_101 = "No record updated.";

  public static final String SYNC_102 = "REMOVE FROM KLAW";

  public static final String SYNC_103 = "ORPHANED";

  // Acl service
  public static final String ACL_ERR_103 = "Failure : Consumer group %s used by another team.";

  public static final String ACL_ERR_104 =
      "Failure : Mentioned Service account used by another team.";

  public static final String ACL_ERR_105 = "Record not found !";

  public static final String ACL_ERR_106 =
      "You are not allowed to approve your own subscription requests.";

  public static final String ACL_ERR_107 = "A delete request already exists.";

  // Acl sync service

  public static final String ACL_SYNC_ERR_102 = "Error in Acl creation. Acl: %s";

  public static final String ACL_SYNC_ERR_103 = "Acl already exists %s";

  public static final String ACL_SYNC_ERR_104 = "Acl added: %s";

  // Analytics service
  public static final String ANALYTICS_101 = "Producer Acls";

  public static final String ANALYTICS_102 = "Consumer Acls";

  public static final String ANALYTICS_103 = "Topics in all clusters";

  public static final String ANALYTICS_104 = "Topics per cluster";

  public static final String ANALYTICS_105 = "Partitions per cluster";

  public static final String ANALYTICS_106 = "Acls per cluster";

  public static final String ANALYTICS_107 = "Requests per day";

  // Base overview service
  public static final String BASE_OVERVIEW_101 = "Not Authorized to see this.";

  public static final String CLUSTER_API_ERR_101 = "Could not get consumer offsets";

  public static final String CLUSTER_API_ERR_102 = "Could not get events for Topic %s";

  public static final String CLUSTER_API_ERR_103 =
      "Could not load topics/acls. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_104 =
      "Could not load topics. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_105 =
      "Could not approve connector request. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_106 =
      "Could not approve topic request. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_107 =
      "Could not approve acl request. AclId - Aiven acl id not found.";

  public static final String CLUSTER_API_ERR_108 =
      "Could not approve acl request. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_109 =
      "Could not retrieve service account details. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_110 =
      "Could not retrieve service accounts. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_111 =
      "Could not post schema. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_112 =
      "Could not validate schema. Please contact Administrator.";

  public static final String CLUSTER_API_ERR_113 = "Could not get schema(s).";

  public static final String CLUSTER_API_ERR_114 = "Could not get Connector Details. %s";

  public static final String CLUSTER_API_ERR_115 = "Could not get KafkaConnectors.";

  public static final String CLUSTER_API_ERR_116 = "Could not get metrics.";

  public static final String CLUSTER_API_ERR_117 =
      "CONFIGURE CLUSTER API SECRET FOR CLUSTER OPERATIONS. klaw.clusterapi.access.base64.secret";

  public static final String CLUSTER_API_ERR_118 =
      "There seems to be a connectivity issue with Cluster Api. Please contact your administrator !!";

  public static final String CLUSTER_API_ERR_119 =
      "There seems to be a connectivity issue with the cluster. Please contact your administrator !!";

  public static final String CLUSTER_API_ERR_120 = "ClientHttpRequestFactory must not be null";

  public static final String CLUSTER_API_ERR_121 = "Connection refused";

  public static final String CLUSTER_API_ERR_122 = "doesn't match connector name in the URL";

  public static final String CLUSTER_API_ERR_123 =
      "Could not approve schema delete request. Please contact Administrator.";

  // Env clusters tenants service
  public static final String ENV_CLUSTER_TNT_ERR_101 =
      "Failure. Please choose a different name. This environment name already exists.";

  public static final String ENV_CLUSTER_TNT_ERR_102 =
      "Failure. Please choose a different name. This cluster name already exists.";

  public static final String ENV_CLUSTER_TNT_ERR_103 = "Failure. Unable to save public key.";

  public static final String ENV_CLUSTER_TNT_ERR_104 =
      "Not allowed to delete this cluster, as there are associated environments.";

  public static final String ENV_CLUSTER_TNT_ERR_105 =
      "Not allowed to delete this environment, as there are associated topics/acls/requests.";

  public static final String ENV_CLUSTER_TNT_ERR_106 =
      "Not allowed to delete this environment, as there are associated connectors/requests.";

  public static final String ENV_CLUSTER_TNT_ERR_107 =
      "Not allowed to delete this environment, as there are associated schemaregistry/requests.";

  public static final String ENV_CLUSTER_TNT_ERR_108 = "Maximum tenants reached.";

  public static final String ENV_CLUSTER_TNT_109 = "Our Organization";

  public static final String ENV_CLUSTER_TNT_110 =
      "Environments pointing to same cluster without a different prefix/suffix regex";

  // Kafka connect service

  public static final String KAFKA_CONNECT_ERR_101 =
      "Failure. Invalid config. tasks.max is not configured";

  public static final String KAFKA_CONNECT_ERR_102 =
      "Failure. Invalid config. connector.class is not configured";

  public static final String KAFKA_CONNECT_ERR_103 =
      "Failure. Invalid config. topics/topics.regex is not configured";

  public static final String KAFKA_CONNECT_ERR_104 =
      "Failure. Invalid config. topics and topics.regex both cannot be configured.";

  public static final String KAFKA_CONNECT_ERR_105 =
      "Failure. Not authorized to request connector for this environment.";

  public static final String KAFKA_CONNECT_ERR_106 =
      "Failure. This connector is owned by a different team.";

  public static final String KAFKA_CONNECT_ERR_107 =
      "Failure. This connector does not exist in base cluster";

  public static final String KAFKA_CONNECT_ERR_108 =
      "Failure. This connector does not exist in %s cluster.";

  public static final String KAFKA_CONNECT_ERR_109 =
      "Failure. Please request for a connector first in %s cluster.";

  public static final String KAFKA_CONNECT_ERR_110 = "Failure. A connector request already exists.";

  public static final String KAFKA_CONNECT_ERR_111 =
      "Failure. This connector already exists in the selected cluster.";

  public static final String KAFKA_CONNECT_ERR_112 = "Unable to create kafka connector.";

  public static final String KAFKA_CONNECT_ERR_113 =
      "You are not allowed to approve your own connector requests.";

  public static final String KAFKA_CONNECT_ERR_114 =
      "Failure. Sorry, you cannot delete this connector, as you are not part of this team.";

  public static final String KAFKA_CONNECT_ERR_115 =
      "Failure. A delete connector request already exists.";

  public static final String KAFKA_CONNECT_ERR_116 = "Failure : Connector not found %s";

  public static final String KAFKA_CONNECT_ERR_117 =
      "Failure. A request already exists for this connector.";

  public static final String KAFKA_CONNECT_ERR_118 =
      "Connector Claim request for all available environments.";

  public static final String KAFKA_CONNECT_ERR_119 =
      "Sorry, your team does not own the connector !!";

  public static final String KAFKA_CONNECT_ERR_120 = "Connector does not exist in any environment.";

  // Kafka connect sync service
  public static final String KAFKA_CONNECT_SYNC_ERR_101 =
      "%s Connector config could not be retrieved.";

  public static final String KAFKA_CONNECT_SYNC_102 = "Connector description";

  public static final String KAFKA_CONNECT_SYNC_ERR_102 =
      "Failure. Please sync up the team of the following connector(s) first in main Sync cluster";

  public static final String KAFKA_CONNECT_SYNC_ERR_103 =
      "Failure. The following connectors are being synchronized with a different team, when compared to main Sync cluster";

  // Request service

  public static final String REQ_SER_ERR_101 = "Failure unable to approve requestId %s";

  public static final String REQ_SER_ERR_102 = "Failure unable to delete requestId %s";

  public static final String REQ_SER_ERR_103 = "Failure Unable to determine target resource.";

  public static final String REQ_SER_ERR_104 = "Failure unable to decline requestId %s";

  // Roles permissions service
  public static final String ROLE_PRM_ERR_101 = "Not Allowed";

  // Saas service
  public static final String SAAS_ERR_101 = "User already exists. You may login.";

  public static final String SAAS_ERR_102 = "Something went wrong. Please try again.";

  public static final String SAAS_ERR_103 = "Tenant does not exist.";

  public static final String SAAS_ERR_104 = "Registration already exists. You may login.";

  public static final String SAAS_ERR_105 = "You cannot request users for default tenant.";

  // Schema service
  public static final String SCHEMA_ERR_101 =
      "You are not allowed to approve your own schema requests.";

  public static final String SCHEMA_ERR_102 = "Failure in uploading schema. Error : %s";

  public static final String SCHEMA_ERR_103 =
      "No topic selected Or Not authorized to register schema for this topic.";

  public static final String SCHEMA_ERR_104 = "Unable to find or access the source Schema Registry";

  public static final String SCHEMA_ERR_105 = "Failure. Invalid json";

  public static final String SCHEMA_ERR_106 =
      "No topic selected Or Not authorized to register schema for this topic.";

  public static final String SCHEMA_ERR_107 = "Failure. A request already exists for this topic.";

  public static final String SERVER_CONFIG_ERR_101 =
      "Failure. Invalid json / incorrect name values. Check tenant and env details.";

  public static final String SERVER_CONFIG_ERR_102 =
      "Failure. Invalid json values. Please check if tenant/environments exist.";

  public static final String SERVER_CONFIG_ERR_103 =
      "Failure. Please check if the environment names exist.";

  public static final String SERVER_CONFIG_ERR_104 =
      "Base Sync Resource %s must be created before being added to the Tenant Model";

  public static final String SERVER_CONFIG_ERR_105 =
      "Resource %s must be created before being added to the Tenant Model";

  public static final String SERVER_CONFIG_ERR_106 =
      "Environments pointing to same kafka cluster is not allowed.";

  // Topic service
  public static final String TOPICS_ERR_101 = "Missing Permissions for this operation.";

  public static final String TOPICS_ERR_102 = "Error in processing advanced topic configs";

  public static final String TOPICS_ERR_103 = "Failure. A delete topic request already exists.";

  public static final String TOPICS_ERR_104 =
      "Failure. Sorry, you cannot delete this topic, as you are not part of this team.";

  public static final String TOPICS_ERR_105 =
      "Failure. There are existing subscriptions for topic. Please get them deleted before.";

  public static final String TOPICS_ERR_106 = "Failure. Topic not found on cluster: %s";

  public static final String TOPICS_ERR_107 = "Failure. A request already exists for this topic.";

  public static final String TOPICS_108 =
      "Topic Claim request for all available environments & related Schemas.";

  public static final String TOPICS_ERR_109 =
      "There are no topics found with this prefix. You may synchronize metadata.";

  public static final String TOPICS_ERR_110 =
      "There are atleast two topics with same prefix owned by different teams.";

  public static final String TOPICS_ERR_111 = "No team found";

  public static final String TOPICS_ERR_112 =
      "You are not allowed to approve your own topic requests.";

  public static final String TOPICS_ERR_113 = "Topic does not exist.";

  public static final String TOPICS_ERR_114 = "Sorry, your team does not own the topic !!";

  // Topic Validation
  public static final String TOPICS_VLD_ERR_101 =
      "Failure. Invalid Topic request type. Possible Value : Create/Promote";

  public static final String TOPICS_VLD_ERR_102 =
      "Failure. Invalid Topic request type. Possible Value : Update";

  public static final String TOPICS_VLD_ERR_103 = "Failure. Invalid Permission Type for request.";

  public static final String TOPICS_VLD_ERR_104 =
      "Failure. Not authorized to request topic for this environment.";

  public static final String TOPICS_VLD_ERR_105 = "Failure. Please fill in topic name.";

  public static final String TOPICS_VLD_ERR_106 = "Failure. Please fill in a valid topic name.";

  public static final String TOPICS_VLD_ERR_107 =
      "Failure. Tenant configuration in Server config is missing. Please configure.";

  public static final String TOPICS_VLD_ERR_108 = "Failure. Team doesn't exist.";

  public static final String TOPICS_VLD_ERR_109 =
      "Failure. This topic is owned by a different team.";

  public static final String TOPICS_VLD_ERR_110 = "Failure. A topic request already exists.";

  public static final String TOPICS_VLD_ERR_111 =
      "Failure. This topic already exists in the selected cluster.";

  public static final String TOPICS_VLD_ERR_112 = "Failure. Base cluster is not configured.";

  public static final String TOPICS_VLD_ERR_113 =
      "Failure. This topic does not exist in %s cluster.";

  public static final String TOPICS_VLD_ERR_114 =
      "Failure. Please request for a topic first in %s cluster.";

  public static final String TOPICS_VLD_ERR_115 = "Topic prefix does not match. %s";

  public static final String TOPICS_VLD_ERR_116 = "Topic suffix does not match. %s";

  public static final String TOPICS_VLD_ERR_117 =
      "Cluster default parameters config missing/incorrect.";

  public static final String TOPICS_VLD_ERR_118 = "Topic regex does not match. %s";

  public static final String TOPICS_VLD_ERR_119 =
      "Topic name: %s is not long enough when prefix and suffix's are excluded. %s minimum are required to be unique.";

  public static final String TOPICS_VLD_ERR_120 =
      "Topic Suffix and Topic Prefix overlap there is a requirement for %s characters minimum to be unique between the prefix and suffix.";

  // Topic overview service
  public static final String TOPIC_OVW_ERR_101 = "Topic does not exist in any environment.";

  public static final String TOPICS_SYNC_ERR_101 = "Error in Topic creation. Topic: ";

  public static final String TOPICS_SYNC_ERR_102 = "Topic update failed : ";

  public static final String TOPICS_SYNC_ERR_103 =
      "Failure. Please sync up the team of the following topic(s) first in main Sync cluster :";

  public static final String TOPICS_SYNC_ERR_104 =
      "Failure. The following topics are being synchronized with a different team, when compared to main Sync cluster : ";

  public static final String TOPICS_SYNC_ERR_105 = "Topic %s %s does not match. %s ";

  public static final String TOPICS_SYNC_ERR_106 =
      "Topic exceeds maximum replication factor %d with %s configured replication factor. ";

  public static final String TOPICS_SYNC_ERR_107 =
      "Topic exceeds maximum partitions %d with %s configured partitions. ";

  public static final String TOPICS_SYNC_ERR_108 = "Topic partitions not configured. ";

  // Teams service
  public static final String TEAMS_ERR_101 = "Team id cannot be empty.";

  public static final String TEAMS_ERR_102 = "Not Authorized to update another SUPERADMIN user.";

  public static final String TEAMS_ERR_103 =
      "Not allowed to delete this team, as there are associated users.";

  public static final String TEAMS_ERR_104 =
      "Not allowed to delete this team, as there are associated topics/acls/requests/connectors.";

  public static final String TEAMS_ERR_105 = "Team cannot be deleted.";

  public static final String TEAMS_ERR_106 =
      "Not Authorized. Cannot delete a user with SUPERADMIN access.";

  public static final String TEAMS_ERR_107 =
      "Not allowed to delete this user, as there are associated requests in the metadata.";

  public static final String TEAMS_ERR_108 = "User cannot be deleted";

  public static final String TEAMS_ERR_109 = "Invalid username/mail id";

  public static final String TEAMS_ERR_110 = "Failure. User already exists.";

  public static final String TEAMS_ERR_111 = "Unable to create the user.";

  public static final String TEAMS_ERR_112 = "Please make sure atleast 2 teams are selected.";

  public static final String TEAMS_ERR_113 =
      "Please select your own team, in the switch teams list.";

  public static final String TEAMS_ERR_114 =
      "Password cannot be updated in ldap/ad authentication mode.";

  public static final String TEAMS_ERR_115 = "User already exists.";

  public static final String TEAMS_ERR_116 = "Invalid tenant provided.";

  public static final String TEAMS_ERR_117 = "Registration already exists.";

  public static final String TEAMS_ERR_118 = "Failure. Something went wrong. Please try later.";

  public static final String TEAMS_ERR_119 = "Failure. Team already exists.";

  // security config
  public static final String SEC_CONFIG_ERR_101 = "Please check if insert scripts are executed.";

  public static final String SEC_CONFIG_ERR_102 = "Please check if tables are created.";

  public static final String MIGRATION_ERR_101 =
      "Unable to complete Migration instructions successfully from %s";

  public static final String SCH_SYNC_ERR_101 =
      "There is no associated Schema environment configured.";

  public static final String SCH_SYNC_ERR_102 = "404 Not Found";
}
