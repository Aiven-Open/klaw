/* Insert Users , pwd: base64 encoded*/

Insert into users(fullname,team,userid,roleid,pwd) values('User1','Team1','uiuser1','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('User2','Team2','uiuser2','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('User3','Team3','uiuser3','USER','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('User4','Team1','uiuser4','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('User5','Team2','uiuser5','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('User6','Team3','uiuser6','ADMIN','dXNlcg==');

Insert into users(fullname,team,userid,roleid,pwd) values('SuperUser','Team2','superuser','SUPERUSER','dXNlcg==');

/* Insert Teams */

Insert into teams(team,app,teammail,teamphone,contactperson) values('Team1','Team1','t1@test.com','+132323232','John Woo');

Insert into teams(team,app,teammail,teamphone) values('Team2','Team2','t1@test2.com','+132323232');

Insert into teams(team,app,teammail,teamphone) values('Team3','Team3','t1@test3.com','+132323232');

/* Insert env */

Insert into env(name,host,port,protocol,type,other_params) values ('DEV','localhost','9092','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,port,protocol,type,other_params) values ('TST','localhost','9094','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,port,protocol,type,other_params) values ('ACC','localhost','9096','PLAINTEXT','kafka','default.partitions=2,max.partitions=4,replication.factor=1');

Insert into env(name,host,port,protocol,type,other_params) values ('PRD','localhost','9098','PLAINTEXT','kafka','default.partitions=2,max.partitions=16,replication.factor=1');

Insert into env(name,host,port,protocol,type) values ('DEV_SCH','localhost','8081','PLAIN','schemaregistry');

/* Insert product and version */

Insert into productdetails(name,version) values ('KafkaWize','4.4');

commit;