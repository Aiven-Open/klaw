/* Create */


Create table if not exists kafkametadb.topic_requests(
	topicname varchar(150),
	partitions varchar(3),
	replicationfactor varchar(2),
	env varchar(50),
	teamname varchar(150),
	appname varchar(150),
	topictype varchar(25),
	requestor varchar(20),
	requesttime timestamp,
	topicstatus varchar(50),
	remarks varchar(500),
	acl_ip varchar(150),
	acl_ssl varchar(200),
	approver varchar(20),
	exectime timestamp,
	PRIMARY KEY(topicname,env)
);

Create table if not exists kafkametadb.topics(
	topicname varchar(150),
	env varchar(50),
	teamname varchar(150),
	appname varchar(150),
	PRIMARY KEY(topicname,env)
);

Create table if not exists kafkametadb.acl_requests(
	req_no varchar(20) PRIMARY KEY,
	topicname varchar(150),
	env varchar(50),
	teamname varchar(150),
	requestingteam varchar(150),
	appname varchar(150),
	topictype varchar(25),
	consumergroup varchar(150),
	requestor varchar(20),
	requesttime timestamp,
	topicstatus varchar(50),
	remarks varchar(500),
	acl_ip varchar(150),
	acl_ssl varchar(200),
	approver varchar(20),
	exectime timestamp
);

Create table if not exists kafkametadb.acls(
	req_no varchar(20) PRIMARY KEY,
	topicname varchar(150),
	env varchar(50),
	teamname varchar(150),
	consumergroup varchar(150),
	topictype varchar(25),
	acl_ip varchar(150),
	acl_ssl varchar(200)
);

Create table if not exists kafkametadb.schema_requests(
	topicname varchar(150),
	env varchar(50),
	teamname varchar(150),
	appname varchar(150),
	requestor varchar(20),
	requesttime timestamp,
	topicstatus varchar(50),
	remarks varchar(500),
	schemafull text,
	approver varchar(20),
	exectime timestamp,
	versionschema varchar(3),
	PRIMARY KEY(topicname,versionschema,env)
);

Create table if not exists kafkametadb.avroschemas(
	topicname varchar(150),
	env varchar(50),
	teamname varchar(150),
	schemafull text,
	versionschema varchar(3),
	PRIMARY KEY(topicname,versionschema,env)
);

Create table if not exists kafkametadb.teams(
	team varchar(150),
	app varchar(150),
	teammail varchar(50),
	teamphone varchar(25),
	contactperson varchar(50),
	PRIMARY KEY(team,app)
);

Create table if not exists kafkametadb.users(
	userid varchar(20),
	pwd varchar(50),
	team varchar(150),
	roleid varchar(20),
	fullname varchar(50),
	PRIMARY KEY(userid)
);

Create table if not exists kafkametadb.env(
	name varchar(50),
	host varchar(20),
	port varchar(6),
	protocol varchar(20),
	type varchar(20),
	keystorelocation varchar(200),
	truststorelocation varchar(200),
	keystorepwd varchar(25),
	keypwd varchar(25),
	truststorepwd varchar(25),
	other_params varchar(250),
	PRIMARY KEY(name)
);

Create table if not exists kafkametadb.productdetails(
	name varchar(9),
	version varchar(10),
	PRIMARY KEY(name)
);

Create table if not exists kafkametadb.activitylog(
	req_no varchar(20) PRIMARY KEY,
	activityname varchar(25),
	activitytype varchar(25),
	activitytime timestamp,
	details varchar(250),
	user varchar(20),
	team varchar(150),
	env varchar(50)
);
