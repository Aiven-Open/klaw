# How to Migrate Data between versions.

Occasionally when introducing new features or addressing tech debt we need to migrate data into new tables or into new columns within the same table.
To address this, Klaw has a method for automatically migrating data to reduce the effort that is required by users to avail of the newest features.

## Creating Migration Instruction class

Create a class that will handle all data migration between the last version release and the target version release.
This class should be in the package "io.aiven.klaw.dao.migration"
The Class should be named with the target release version in the name with the dots replaced by 'x'.
examples: MigrateData2x0x0 (version 2.0.0),MigrateData2x5x0 (version 2.5.0),MigrateData3x0x2(version 3.0.2)

## Annotations

This class should contain a class level annotation "@DataMigration" and takes two variables.
The version in regular format e.g. "2.2.0" and the order in which the migration should be executed. The order allows for explicit control of the order of executing instructions.
This prevents issues with executing instructions out of order and not having data migrated correctly.
At the class level it should also be annotated with the Spring Boot "@Configuration" to ensure that all the autowired objects are correctly initialised.

The third annotation is a method level annotation called "@MigrationRunner" which signifies the method to invoke to execute the migration instructions.

## How does it work ?

The MigrationUtility invokes a PostConstruct method, this means that after Klaw is successfully initialised including all liquibase operations to make changes to the database.
This PostConstruct then searches the package "io.aiven.klaw.dao.migration" to find all initialised classes annotated with @DataMigration.
The returned list is stripped of any already applied Migration instructions, for example if upgrading from version 2.5.0 to 3.0.0 the migration included in 2.2.0 would be excluded as it has already been applied before this.
The classes are then ordered by the order number and the method in each class annotated by MigrationRunner is invoked.
After each successfully invoked action a boolean is returned and the DataVersion Table is updated with the information that the migration for that version has been successfully completed.
If at any point a failure occurs a Klaw error is returned with the class that failed to execute and when re-running the migration it will pick up its data migration from the last successfully run migration instruction.
