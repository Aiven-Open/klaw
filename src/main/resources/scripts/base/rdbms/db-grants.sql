/* Postgres */

Create database klaw;

Create user kafkauser;

Alter user kafkauser with encrypted password 'klaw';

Grant all privileges on database klaw to kafkauser;


/* Mysql */

Create database klaw;

Create user 'kafkauser'@'localhost' identified by 'klaw';

GRANT SELECT,INSERT,UPDATE,DELETE on klaw.* to kafkauser@localhost;

Commit;