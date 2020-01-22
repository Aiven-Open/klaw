ALTER TABLE kafkametadb.env
ADD other_params varchar(250);


Insert into kafkametadb.env(name,host,port,protocol,type,other_params) values ('DEV','localhost','9092','PLAIN','kafka','default.paritions=2,max.partitions=4,replication.factor=1');

Insert into kafkametadb.env(name,host,port,protocol,type,other_params) values ('TST','tesstserver','9092','PLAIN','kafka','default.paritions=2,max.partitions=4,replication.factor=1');

Insert into kafkametadb.env(name,host,port,protocol,type,other_params) values ('TST_SSL','tesstserver','9093','SSL','kafka','default.paritions=2,max.partitions=4,replication.factor=1');

