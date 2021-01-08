/* Insert Users , pwd: base64 encoded*/

Insert into users(fullname,team,userid,roleid,pwd) values('Gary','Octopus','gary','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('Will','Seahorses','will','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('John','Starfish','john','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('Cris','Octopus','cris','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('Noah','Seahorses','noah','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('Alex','Starfish','alex','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('SuperUser','Seahorses','superuser','SUPERUSER','dXNlcg==');

/* Insert Teams */

Insert into teams(team,app,teammail,teamphone,contactperson) values('Octopus','Octopus','Octopus@company.com','+132323232','John');

Insert into teams(team,app,teammail,teamphone,contactperson) values('Seahorses','Seahorses','Seahorses@company.com','+132323232','Will');

Insert into teams(team,app,teammail,teamphone,contactperson) values('Starfish','Starfish','Starfish@company.com','+132323232','Alex');

/* Insert env */

Insert into env(name,host,protocol,type,other_params) values ('DEV','localhost:6092','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,protocol,type,other_params) values ('TST','localhost:7092','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,protocol,type,other_params) values ('ACC','localhost:8092','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,protocol,type,other_params) values ('PRD','localhost:9092','PLAINTEXT','kafka','default.partitions=2,max.partitions=16,replication.factor=1');

Insert into env(name,host,protocol,type) values ('DEV_SCH','localhost:8081','PLAINTEXT','schemaregistry');

/* Insert product and version */

Insert into productdetails(name,version) values ('KafkaWize','4.4');

commit;