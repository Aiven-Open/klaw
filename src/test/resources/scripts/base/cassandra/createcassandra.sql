/* Create */

CREATE KEYSPACE if not exists kafkamanagementapi  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

Create table if not exists kafkamanagementapi.topic_requests(topicname text,  partitions text,	replicationfactor text,env text,teamname text,appname text,topictype text, 	requestor text,requesttime timestamp,topicstatus text ,remarks text,acl_ip text, acl_ssl text,approver text,exectime timestamp,  PRIMARY KEY(topicname,env));

Create table if not exists kafkamanagementapi.topics(topicname text, partitions text,	replicationfactor text, env text, teamname text, appname text, others text, PRIMARY KEY(topicname, env));

Create table if not exists kafkamanagementapi.acl_requests(req_no text PRIMARY KEY,topicname text,env text,teamname text,requestingteam text,appname text,topictype text,consumergroup text,requestor text,requesttime timestamp,topicstatus text,remarks text,acl_ip text,acl_ssl text,approver text,exectime timestamp, acltype text);

Create table if not exists kafkamanagementapi.acls(req_no text PRIMARY KEY, topicname text, env text,teamname text,consumergroup text, topictype text, acl_ip text, acl_ssl text);

Create table if not exists kafkamanagementapi.schema_requests(topicname text,env text,teamname text,appname text,requestor text,requesttime timestamp,topicstatus text,remarks text,schemafull text,approver text,exectime timestamp,versionschema text,PRIMARY KEY(topicname,versionschema,env));

Create table if not exists kafkamanagementapi.avroschemas(req_no text, topicname text, env text,teamname text,schemafull text, versionschema text,PRIMARY KEY(topicname,versionschema,env));

Create table if not exists kafkamanagementapi.teams(team text,app text,teammail text,teamphone text,contactperson text,PRIMARY KEY(team));

Create table if not exists kafkamanagementapi.users(userid text,pwd text,team text,roleid text,fullname text, mailid text, PRIMARY KEY(userid));

Create table if not exists kafkamanagementapi.env(name text,host text,port text,protocol text,type text,keystorelocation text,truststorelocation text,keystorepwd text,keypwd text,truststorepwd text, other_params text, PRIMARY KEY(name));

Create table if not exists kafkamanagementapi.productdetails(name text,version text,licensekey text,PRIMARY KEY(name));

Create table if not exists kafkamanagementapi.activitylog(req_no text PRIMARY KEY, activityname text, activitytype text, activitytime timestamp, details text, user text, team text, env text);