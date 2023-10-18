# Klaw · [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![CodeQL](https://github.com/Aiven-Open/klaw/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/Aiven-Open/klaw/actions/workflows/codeql.yml) ![CI](https://github.com/Aiven-Open/klaw/actions/workflows/workflow-merge-to-main.yaml/badge.svg)

Klaw is fully opensource. 🥳

Klaw is a self-service Apache Kafka® Topic Management/Governance tool/portal. It is a web application which automates the process of creating and browsing Apache Kafka topics, acls, avro schemas, connectors by introducing roles/authorizations to users of various teams of an organization.

With several downloads every week, many more companies which did not adopt a solution, can happily move away from managing Apache Kafka configs in excelsheets, confluence, wiki pages, git etc.

## Built With

- Bootstrap UI, Angular, Javascript, HTML, CSS
- [Maven](https://maven.apache.org/) - Dependency Management
  - A maven wrapper, mvnw is included in the Klaw Git Repo
- Java (Jdk 17, 21), Spring boot 3, Spring security, SQL, Apache Kafka Admin client

## Versioning

For the versions available, see the [tags on this repository](https://github.com/aiven/klaw/tags).

## Architecture:

![Architecture](https://github.com/aiven/klaw/blob/main/arch.png)

## Features:

- Topics (approval): Create, Update, Delete, Promote
  - React UI is now by default enabled in the configuration. If react based assets are already built, new UI should be visible.
  - React UI - New look and feel for Browse topics, Create topic Request, Acl Request, Schema Request and Connector Request
  - React UI - New look and feel for Approving Topics, ACLs, Schemas and Connectors
  - React UI - New look and feel for viewing 'My team's Requests' for topics, ACLs, Schemas and Connectors
  - Topic Requests are now editable after creation and so you can alter them to correct any mistakes.
- Acls (approval): Create,Delete
  - React UI - Create Acl Request
  - Service accounts created for ACLs are assigned to the creating team
- Connectors (approval): Create
  - Any connector can be created as long as the required plugin libraries are installed on the server.
  - Connector configuration now encrypts password configuration for security.
- Avro Schemas (approval): Create
  - View all available versions of the subjects per topic
  - React UI - Create Schema Request
  - Pre validation of Schemas compatibility on Schema request creation
- Topic Overview :

  - React UI now provides a redesigned Topic Overview and all of its capabilities to enhance the user experience.
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

- Synchronization (migration) from and to Apache Kafka clusters (Topics, Acls, Schemas and Connectors)
- Reconciliation and email notifications on differences between Klaw and Clusters
- Restore configuration (topics, acls)

- Login
  - Azure / Active directory integration
  - Single Sign-on (OAuth2)
  - Based on database
- Configure Clusters and Environments

  - Clusters can be created connecting to Apache Kafka clusters. (Cluster Management Api should be configured)
  - Environments are wrappers over clusters, enforcing flexible configs like prefix, suffix etc
  - Kafka Rest Proxy can be added to the cluster to store the rest proxy url information.

- Users, Teams & Authorizations

  - Configurable users, teams
  - More than 35 permissions
  - Configurable roles (Roles can be pulled from AD for authorization)
  - Ability to switch between different teams

- Topic naming conventions

  - Enforce prefix, suffixes and/or regex per environment

- Excel report (for your team and all teams, depending on the role)

  - Topics per cluster (for teams)
  - Partitions per cluster
  - Overall topics in all clusters
  - Acls per cluster (for teams)
  - Producer Acls (for teams)
  - Consumer Acls (for teams)
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

## Documentation

- User documentation : https://klaw-project.io/docs
- Technical documentation for contributors:
  - general documentation: [`./docs`](./docs)
  - specific documentation for the React app `coral`: [`./coral/docs`](./coral/docs)
- [Contribute to improving the Klaw documentation here](https://github.com/aiven/klaw-docs)

## Questions and Answers

We encourage everyone to ask question, if you have a question someone else definitely does too.

- [Klaw Community Support](https://aiven.io/community/forum/c/open-source/12)
- [Klaw GitHub discussions](https://github.com/aiven/klaw/discussions)

The Klaw team will cross post questions and answers across both forums. The Klaw Community Support forum, which is hosted and supported by Aiven directly, has more visibility for other interested parties, so we would encourage questions to be asked there. We will respond and answer questions in both GitHub discussions and the Community Forum.

## Install

### Manual

##### Use the Maven Wrapper

- ./mvnw clean install

##### Use your own Maven

- mvn clean install

Optional step : For new React UI assets, make sure pnpm is pre installed which is required to build coral assets.

Builds two artifacts core/target/klaw-<version>.jar and cluster-api/target/cluster-api-<version>.jar

and follow steps defined at https://klaw-project.io/docs or run the binaries like below

java -jar core/target/klaw-<version>.jar

java -jar cluster-api/target/cluster-api-<version>.jar --spring.config.location=cluster-api/target/classes/application.properties

### With `make`

1. Clone this repo containing two maven modules (core and cluster-api) : `git clone git@github.com:aiven/klaw.git`
2. Run `make` to install dependencies and setup both Klaw Core and the Klaw-Cluster-Api
3. [Optional] edit any configs using `make edit-core-config` for Klaw Core or `make edit-cluster-api-config` for Klaw-Cluster-API
4. To run, you can use `make run-core` and `make run-cluster-api` in different terminal windows or you can run `make -j2 run-core run-cluster-api` to execute both (NOTE: this will mix output and make debugging harder)

### With `docker`

#### Dockerhub

Each official release is available on docker in the two repositories.
The [klaw-core](https://hub.docker.com/r/aivenoy/klaw-core) docker hub page contains an example docker-compose file which will help download and deploy Klaw.
The [cluster-api](https://hub.docker.com/r/aivenoy/klaw-cluster-api) can also be found separately on docker hub.

#### Locally

This project also has the ability to build and deploy local docker images using scripts provided in Klaw/docker-scripts .

## License

Klaw is licensed under the Apache license, version 2.0. Full license text is
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

Copyright (c) 2023 Aiven Oy and klaw project contributors.
