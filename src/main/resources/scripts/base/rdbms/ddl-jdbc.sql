/* Create */

/* For default  timestamps warnings: You can try disabling strict mode. mysql -u root -p -e "SET GLOBAL sql_mode = 'NO_ENGINE_SUBSTITUTION';" or set global sql_mode=''; */

/* mysql -u root -p -e "SET GLOBAL sql_mode = 'NO_ENGINE_SUBSTITUTION';" */

/*
 If you enable liquibase, the below scripts don't have to be execute manually, however configured user (in application.properties) should have the privileges to create tables
*/

Create table if not exists kwtopicrequests(
	topicid INT NOT NULL,
	topicname varchar(150),
	partitions INT,
	replicationfactor varchar(2),
	env varchar(50),
	teamid int not null,
	appname varchar(150),
	topictype varchar(25),
	requestor varchar(300),
	requesttime timestamp,
	topicstatus varchar(50),
	remarks varchar(500),
	approver varchar(300),
	exectime timestamp,
	otherparams varchar(150),
	description varchar(100),
	tenantid INT NOT NULL,
	PRIMARY KEY(topicid, tenantid)
);

Create table if not exists kwtopics(
	topicid INT NOT NULL,
	topicname varchar(150),
	partitions INT,
    replicationfactor varchar(2),
	env varchar(50),
	teamid int not null,
	appname varchar(150),
	otherparams varchar(150),
	description varchar(100),
	documentation text,
	history text,
	jsonparams text,
	tenantid INT NOT NULL,
	PRIMARY KEY(topicid, tenantid)
);

Create table if not exists kwaclrequests(
	aclid INT not null,
	topicname varchar(150),
	env varchar(50),
	teamid int not null,
	requestingteam int not null,
	appname varchar(150),
	topictype varchar(25),
	consumergroup varchar(150),
	requestor varchar(300),
	requesttime timestamp,
	topicstatus varchar(50),
	remarks varchar(500),
	aclip varchar(500),
	aclssl text,
	approver varchar(300),
	exectime timestamp,
	acltype varchar(10),
	aclpatterntype varchar(20),
	aclresourcetype varchar(15),
	transactionalid varchar(50),
	tenantid INT NOT NULL,
	otherparams varchar(50),
	PRIMARY KEY(aclid, tenantid)
);

Create table if not exists kwacls(
	aclid INT not null,
	topicname varchar(150),
	env varchar(50),
	teamid int not null,
	consumergroup varchar(150),
	topictype varchar(25),
	aclip varchar(150),
	aclssl varchar(200),
	aclpatterntype varchar(20),
    aclresourcetype varchar(15),
    transactionalid varchar(50),
    otherparams varchar(50),
    tenantid INT NOT NULL,
    jsonparams text,
    PRIMARY KEY(aclid, tenantid)
);

Create table if not exists kwschemarequests(
	avroschemaid INT not null,
	topicname varchar(150),
	env varchar(50),
	teamid int not null,
	appname varchar(150),
	requestor varchar(300),
	requesttime timestamp,
	topicstatus varchar(50),
	requesttype varchar(25),
	remarks varchar(500),
	schemafull text,
	approver varchar(300),
	exectime timestamp,
	versionschema varchar(3),
	tenantid INT NOT NULL,
	PRIMARY KEY(avroschemaid, tenantid)
);

Create table if not exists kwavroschemas(
	avroschemaid INT not null,
	topicname varchar(150),
	env varchar(50),
	teamid int not null,
	schemafull text,
	versionschema varchar(3),
	jsonparams text,
	tenantid INT NOT NULL,
	PRIMARY KEY(avroschemaid, tenantid)
);

Create table if not exists kwkafkaconnectorrequests(
	connectorid INT not null,
	connectorname varchar(150),
	env varchar(50),
	teamid int not null,
	connectortype varchar(25),
	requestor varchar(300),
	requesttime timestamp,
	connectorstatus varchar(50),
	connectorconfig text,
	approver varchar(300),
	exectime timestamp,
	otherparams varchar(150),
	description varchar(100),
	remarks varchar(500),
	tenantid INT NOT NULL,
	PRIMARY KEY(connectorid, tenantid)
);

Create table if not exists kwkafkaconnector(
	connectorid INT not null,
	connectorname varchar(150),
	env varchar(50),
	teamid int not null,
	connectorconfig text,
	description varchar(100),
	documentation text,
	history text,
	tenantid INT NOT NULL,
	PRIMARY KEY(connectorid, tenantid)
);

Create table if not exists kwusers(
	userid varchar(300),
	pwd varchar(100),
	teamid int not null,
	roleid varchar(20),
	fullname varchar(50),
	mailid varchar(150),
	otherparams varchar(150),
	tenantid INT not null,
	PRIMARY KEY(userid)
);

Create table if not exists kwregisterusers(
	userid varchar(300),
	pwd varchar(100),
	teamid int not null,
	roleid varchar(20),
	fullname varchar(50),
	mailid varchar(300),
	status varchar(25),
	registeredtime timestamp,
	approver varchar(300),
	registrationid varchar(100),
	tenantid INT,
	PRIMARY KEY(userid)
);

Create table if not exists kwproductdetails(
	name varchar(9),
	version varchar(10),
	PRIMARY KEY(name)
);

Create table if not exists kwactivitylog(
	kwreqno INT not null,
	activityname varchar(25),
	activitytype varchar(25),
	activitytime timestamp,
	details varchar(250),
	userid varchar(300),
	teamid int not null,
	env varchar(50),
	tenantid INT NOT NULL,
	PRIMARY KEY(kwreqno, tenantid)
);

Create table if not exists kwproperties(
	kwkey varchar(75),
	kwvalue varchar(3000),
	kwdesc varchar(300),
	tenantid INT NOT NULL,
	PRIMARY KEY(kwkey, tenantid)
);


Create table if not exists kwtenants(
	tenantid INT PRIMARY KEY,
	tenantname varchar(25) unique NOT NULL,
    tenantdesc varchar(100),
    contactperson varchar(150),
    intrial varchar(10),
    isactive varchar(10),
    orgname varchar(50),
    licenseexpiry timestamp
);


Create table if not exists kwteams(
    teamid int not null,
	team varchar(30) not null,
	app varchar(150),
	teammail varchar(300),
	teamphone varchar(25),
	contactperson varchar(50),
	requesttopicsenvs varchar(75),
    restrictionsobj varchar(150),
	otherparams varchar(150),
    tenantid INT not null,
	PRIMARY KEY(teamid, tenantid)
);

Create table if not exists kwenv(
    id varchar(3) not null,
	envname varchar(10) NOT NULL,
	envtype varchar(20),
    clusterid INT,
    tenantid INT NOT NULL,
    otherparams varchar(250),
    stretchcode varchar(15),
    envexists varchar(5) default 'true',
    envstatus varchar(25),
	PRIMARY KEY(id, tenantid)
);

Create table if not exists kwclusters(
	clusterid INT not null,
	clustername varchar(25) NOT NULL,
	bootstrapservers varchar(250),
	protocol varchar(30),
	clustertype varchar(20),
	tenantid INT not null,
	sharedcluster varchar(5) default 'false',
	publickey text,
	cstatus varchar(25),
	PRIMARY KEY(clusterid, tenantid)
);

Create table if not exists kwrolespermissions(
    id INT not null,
	roleid varchar(20),
    permission varchar(50),
    description varchar(150),
    tenantid INT NOT NULL,
    PRIMARY KEY(id, tenantid)
);


Create table if not exists kwkafkametrics(
    metricsid INT PRIMARY KEY,
    metricstime varchar(20),
    env varchar(50),
    metricstype varchar(150),
    metricsname varchar(150),
    metricsattributes text
);

commit;