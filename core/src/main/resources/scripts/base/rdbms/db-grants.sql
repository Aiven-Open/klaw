/* Postgres */

Create database klaw;

Create user kafkauser;

Alter user kafkauser with encrypted password 'klaw';

Grant all privileges on database klaw to kafkauser;


/* Mysql */

/* For default  timestamps warnings: You can try disabling strict mode. mysql -u root -p -e "SET GLOBAL sql_mode = 'NO_ENGINE_SUBSTITUTION';" or set global sql_mode=''; */

/* mysql -u root -p -e "SET GLOBAL sql_mode = 'NO_ENGINE_SUBSTITUTION';" */

Create database klaw;

Create user 'kafkauser'@'localhost' identified by 'klaw';

GRANT SELECT,INSERT,UPDATE,DELETE on klaw.* to kafkauser@localhost;

Commit;