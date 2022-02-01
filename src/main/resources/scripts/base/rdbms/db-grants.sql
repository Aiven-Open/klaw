/* Postgres */

Create database kafkawize;

Create user kafkauser;

Alter user kafkauser with encrypted password 'kafkawize';

Grant all privileges on database kafkawize to kafkauser;


/* Mysql */

Create database kafkawize;

Create user 'kafkauser'@'localhost' identified by 'kafkawize';

GRANT SELECT,INSERT,UPDATE,DELETE on kafkawize.* to kafkauser@localhost;

Commit;