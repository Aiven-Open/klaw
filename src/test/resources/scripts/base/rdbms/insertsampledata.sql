/* Insert Tenants */

Insert into kwtenants(tenantid,tenantname,tenantdesc, intrial, isactive, licenseexpiry) values(101,'default','Default tenant','false', 'true', '2030-10-29 10:31:01');

/* Insert RolesPermissions */

Insert into kwrolespermissions(id,roleid,permission,description) values(1,'USER','REQUEST_CREATE_TOPICS','Permission to request for Topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(2,'USER','REQUEST_CREATE_SUBSCRIPTIONS','Permission to request for Producer or consumer subscriptions');

Insert into kwrolespermissions(id,roleid,permission,description) values(3,'USER','REQUEST_DELETE_TOPICS','Permission to request for deletion of topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(4,'USER','REQUEST_DELETE_SUBSCRIPTIONS','Permission to request for deletion of subscriptions');

Insert into kwrolespermissions(id,roleid,permission,description) values(5,'USER','REQUEST_CREATE_SCHEMAS','Permission to request for Schemas');

Insert into kwrolespermissions(id,roleid,permission,description) values(6,'USER','REQUEST_DELETE_SCHEMAS','Permission to request for deletion of schemas');

Insert into kwrolespermissions(id,roleid,permission,description) values(7,'APPROVER','APPROVE_TOPICS','Permission to approve topics requests');

Insert into kwrolespermissions(id,roleid,permission,description) values(8,'APPROVER','APPROVE_SUBSCRIPTIONS','Permission to approve producer or consumer subscriptions');

Insert into kwrolespermissions(id,roleid,permission,description) values(9,'APPROVER','APPROVE_SCHEMAS','Permission to approve schemas');

Insert into kwrolespermissions(id,roleid,permission,description) values(10,'ADMIN','SYNC_TOPICS','Permission to Synchronize topics From Cluster');

Insert into kwrolespermissions(id,roleid,permission,description) values(11,'ADMIN','SYNC_SUBSCRIPTIONS','Permission to Synchronize acls From Clsuter');

Insert into kwrolespermissions(id,roleid,permission,description) values(12,'ADMIN','ADD_EDIT_DELETE_TEAMS','Permission to add modify delete teams');

Insert into kwrolespermissions(id,roleid,permission,description) values(13,'ADMIN','ADD_EDIT_DELETE_USERS','Permission to add modify delete users');

Insert into kwrolespermissions(id,roleid,permission,description) values(14,'SUPERADMIN','ADD_EDIT_DELETE_CLUSTERS','Permission to add modify delete clusters');

Insert into kwrolespermissions(id,roleid,permission,description) values(15,'SUPERADMIN','ADD_EDIT_DELETE_ENVS','Permission to add modify delete environments');

Insert into kwrolespermissions(id,roleid,permission,description) values(16,'SUPERADMIN','ADD_EDIT_DELETE_TENANTS','Permission to add modify delete tenants');

Insert into kwrolespermissions(id,roleid,permission,description) values(17,'SUPERADMIN','VIEW_EDIT_ALL_ENVS_CLUSTERS_TENANTS','Permission to view all tenants etc');

Insert into kwrolespermissions(id,roleid,permission,description) values(18,'SUPERADMIN','REQUEST_CREATE_SUBSCRIPTIONS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(19,'SUPERADMIN','REQUEST_CREATE_TOPICS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(20,'SUPERADMIN','APPROVE_TOPICS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(21,'SUPERADMIN','APPROVE_SUBSCRIPTIONS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(22,'SUPERADMIN','ADD_EDIT_DELETE_TEAMS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(23,'SUPERADMIN','ADD_EDIT_DELETE_USERS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(24,'SUPERADMIN','SYNC_TOPICS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(25,'SUPERADMIN','SYNC_SUBSCRIPTIONS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(26,'SUPERADMIN','REQUEST_CREATE_SCHEMAS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(27,'SUPERADMIN','APPROVE_SCHEMAS','');

Insert into kwrolespermissions(id,roleid,permission,description) values(28,'SUPERADMIN','SHUTDOWN_KAFKAWIZE','Permission to stop kafkawize');

Insert into kwrolespermissions(id,roleid,permission,description) values(29,'SUPERADMIN','UPDATE_PERMISSIONS','Permission to update permissions');

Insert into kwrolespermissions(id,roleid,permission,description) values(30,'SUPERADMIN','ADD_EDIT_DELETE_ROLES','');

Insert into kwrolespermissions(id,roleid,permission,description) values(31,'SUPERADMIN','SYNC_BACK_TOPICS','Permission to sync back topics to cluster');

Insert into kwrolespermissions(id,roleid,permission,description) values(32,'SUPERADMIN','SYNC_BACK_SUBSCRIPTIONS','Permission to sync back subscriptions to cluster');

Insert into kwrolespermissions(id,roleid,permission,description) values(33,'SUPERADMIN','UPDATE_SERVERCONFIG','Permission to update server config, properties');

Insert into kwrolespermissions(id,roleid,permission,description) values(34,'SUPERADMIN','FULL_ACCESS_USERS_TEAMS_ROLES','With this permissions, it is possible to assign any role to any user., view all teams of all tenants.');

Insert into kwrolespermissions(id,roleid,permission,description) values(35,'SUPERADMIN','ALL_TEAMS_REPORTS','Possible to view and download reports of all teams');

Insert into kwrolespermissions(id,roleid,permission,description) values(36,'SUPERADMIN','APPROVE_ALL_REQUESTS_TEAMS','To approve any requests of all teams within same tenant');

Insert into kwrolespermissions(id,roleid,permission,description) values(37,'USER','VIEW_TOPICS','View topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(38,'APPROVER','VIEW_TOPICS','View topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(39,'ADMIN','VIEW_TOPICS','View topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(40,'SUPERADMIN','VIEW_TOPICS','View topics');

Insert into kwrolespermissions(id,roleid,permission,description) values(41,'USER','REQUEST_CREATE_CONNECTORS','Permission to request for Connectors');

Insert into kwrolespermissions(id,roleid,permission,description) values(42,'APPROVER','APPROVE_CONNECTORS','Permission to approve kafka connector requests');

Insert into kwrolespermissions(id,roleid,permission,description) values(43,'USER','VIEW_CONNECTORS','View connectors');

Insert into kwrolespermissions(id,roleid,permission,description) values(44,'USER','REQUEST_DELETE_CONNECTORS','');

/* Insert kwclusters */

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (1,'DEV_CLUSTER','localhost:6092','PLAINTEXT','kafka');

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (2,'TST_CLUSTER','localhost:7093','SSL','kafka');

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (3,'ACC_CLUSTER','localhost:8094','SASL_PLAIN','kafka');

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (4,'PRD_CLUSTER','localhost:9095','SASL_SSL-PLAINMECHANISM','kafka');

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (5,'PREPRD_CLUSTER','localhost:5092','SASL_SSL-GSSAPIMECHANISM','kafka');

Insert into kwclusters(clusterid,clustername,bootstrapservers,protocol,clustertype) values (6,'DEV_SCH','localhost:8081','PLAINTEXT','schemaregistry');

/* Insert kwenv. clusterid refers to kwclusters table. stretchcode refers to continous deployment sequence ex: DEV->TST->ACC-PRD, tenantid refers to kwtenants cluster. It should be only one tenant id (not comma seperated) */
/* If stretchcode is null, it will become an independent environment*/

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid,otherparams) values (1,'DEV','kafka',1,'','101','default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=');

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid,otherparams) values (2,'TST','kafka',2,'','101','default.partitions=2,max.partitions=4,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=');

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid,otherparams) values (3,'ACC','kafka',3,'','101','default.partitions=2,max.partitions=4,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=');

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid,otherparams) values (4,'PRD','kafka',4,'','101','default.partitions=2,max.partitions=8,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=');

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid,otherparams) values (5,'PRE','kafka',5,'','101','default.partitions=2,max.partitions=16,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=');

Insert into kwenv(id,envname,envtype,clusterid,stretchcode,tenantid) values (6,'DEV_SCH','schemaregistry',6,'','101');

/* Insert Users , pwd: jasypt encrypted. secret key in application props user = OJielnWysqaTKrOFkPo/Yg== */

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('krish','Octopus','krish','USER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('murali','Seahorses','murali','USER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('andy','Starfish','andy','USER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('gary','Octopus','gary','APPROVER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('mary','Seahorses','mary','APPROVER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('vijay','Starfish','vijay','APPROVER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('william','Seahorses','william','SUPERUSER','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('chris','Octopus','chris','SUPERADMIN','OJielnWysqaTKrOFkPo/Yg==','kafkawize@gmail.com');

Insert into kwusers(fullname,team,userid,roleid,pwd,mailid) values('superadmin','INFRATEAM','superadmin','SUPERADMIN','OJielnWysqaTKrOFkPo/Yg==','admin@company.com');


/* Insert Teams */

Insert into kwteams(team,app,teammail,teamphone,contactperson, tenantid) values('Octopus','Octopus','Octopus@myorg.com','00132323232','Octopus','101');

Insert into kwteams(team,app,teammail,teamphone,contactperson, tenantid) values('Seahorses','Seahorses','Seahorses@myorg.com','00132323232','Seahorses','101');

Insert into kwteams(team,app,teammail,teamphone,contactperson, tenantid) values('Starfish','Starfish','Starfish@myorg.com','00132323232','Starfish','101');

Insert into kwteams(team,app,teammail,teamphone,contactperson, tenantid) values('DEFAULT-TEAM','','DEFAULT-TEAM@company.com','0033323232323','DEFAULT-TEAM','101');

/* Insert product and version */

Insert into kwproductdetails(name,version) values ('Kafkawize','5.0.4');

commit;
