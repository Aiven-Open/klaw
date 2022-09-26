# Klaw

Klaw is fully opensource. 🥳 

Klaw is a self-service Apache Kafka® Topic Management/Governance tool/portal. It is a web application which automates the process of creating and browsing Apache Kafka topics, acls, avro schemas, connectors by introducing roles/authorizations to users of various teams of an organization.

With several downloads every week, many more companies which did not adopt a solution, can happily move away from managing Apache Kafka configs in excelsheets, confluence, wiki pages, git etc. 

## Built With

* Bootstrap UI, Angular, Javascript, HTML, CSS 
* [Maven](https://maven.apache.org/) - Dependency Management
* Java, Spring boot, Spring security, SQL, Apache Kafka Admin client 

## Versioning

For the versions available, see the [tags on this repository](https://github.com/aiven/klaw/tags).

## Architecture:

![Architecture](https://github.com/aiven/klaw/blob/main/arch.png)

## Dependency project:

### ClusterApi
https://github.com/aiven/klaw-cluster-api

## Features:

- Topics (approval): Create, Update, Delete, Promote
- Acls (approval):  Create,Delete
- Connectors (approval): Create
  - Any connector can be created as long as the required plugin libraries are installed on the server.  
- Avro Schemas (approval): Create
  - View all available versions of the subjects per topic
- Topic Overview :
  - Topic Config
  - Promote
  - Literal and Prefixed subscriptions
  - Topic documentation
  - Consumer offsets/ lag
  - View topic contents

- View created, completed, declined, all Topic requests
- View created, completed, declined, all Acl requests
- View created, completed, declined, all Connector requests
- View created, completed, declined, all Avro schema requests

- Synchronization from and to Apache Kafka clusters
- Reconciliation and email notifications on differences between Klaw and Clusters
- Restore configuration (topics, acls)

- Login 
  - Active directory integration
  - Single Sign-on (OAuth2)
  - Based on database
  
- Configure Clusters and Environments
  - Clusters can be created connecting to Apache Kafka clusters. (Cluster Management Api should be configured)
  - Environments are wrappers over clusters, enforcing flexible configs like prefix, suffix etc

- Users, Teams & Authorizations
  - Configurable users, teams
  - More than 35 permissions
  - Configurable roles (Roles can be pulled from AD for authorization)

- Topic naming conventions
  - Enforce prefix and suffixes per environment

- Excel report (for your team and all teams, depending on the role)
  - Topics per cluster (for teams)
  - Partitions per cluster
  - Overall topics in all clusters
  - Acls per cluster (for teams)
  - Producer Acls  (for teams)
  - Consumer Acls  (for teams)
  - Consumer groups of all environments
  - Requests per day

- Analytics
  - View charts of topics, partitions, acls, requests

- Multi tenancy
  - Each tenant can manage their topics with their own teams in isolation.
  - Every tenant can have their own set of Kafka environments, and users
    of one tenant cannot view/access topics, acls or any from other tenants.
  - It provides an isolation avoiding any security breach.

- Apache Kafka Connectivity
  - PLAINTEXT, SSL, SASL

- Audit
  - All topic, acl, schema and connector requests

- Email notifications when
  - requests are created, approved, declined
  - users are created, approved

- Help Wizard to setup Klaw

- Documentation : https://klaw-project.io/docs

## Install

mvn clean install

and follow steps defined at https://klaw-project.io/docs

## License

Klaw is licensed under the Apache license, version 2.0.  Full license text is
available in the [LICENSE.md](LICENSE.md) file.

Please note that the project explicitly does not require a CLA (Contributor
License Agreement) from its contributors.

## Contact

Bug reports and patches are very welcome, please post them as GitHub issues
and pull requests at https://github.com/aiven/klaw . Any possible
vulnerabilities or other serious issues should be reported directly to the
maintainers <opensource@aiven.io>.

## Trademark

Apache Kafka is either a registered trademark or trademark of the Apache Software Foundation in the United States and/or other countries. 
All product and service names used in this page are for identification purposes only and do not imply endorsement.

## Credits

Klaw (formerly Kafkawize) is maintained by, [Aiven](https://aiven.io/) open source developers.


Recent contributors are listed on the GitHub project page,
https://github.com/aiven/klaw/graphs/contributors

Copyright (c) 2022 Aiven Oy and klaw project contributors.
