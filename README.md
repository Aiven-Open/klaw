# Kafkawize

Kafkawize is now fully opensource. 🥳 All the features of the Kafkawize are now available at no cost. 

Kafkawize is a Self service Apache Kafka Topic Management/Governance tool/portal. It is a web application which automates the process of creating and browsing Kafka topics, acls, avro schemas, connectors by introducing roles/authorizations to users of various teams of an organization.

Withe several downloads every week, many more companies which did not adopt a solution, can happily move away from managing kafka configs in excelsheets and wiki pages. 

[![Features](https://yt-embed.herokuapp.com/embed?v=i7nmi-lovgA)](https://www.youtube.com/watch?v=i7nmi-lovgA "Create a kafka topic")
=======

## Built With

* Bootstrap UI, Angular, Javascript, HTML, CSS 
* [Maven](https://maven.apache.org/) - Dependency Management
* Java, Spring boot, Spring security, SQL, Kafka Admin client 

## Versioning

For the versions available, see the [tags on this repository](https://github.com/muralibasani/kafkawize/tags).

## Authors

* **Muralidhar Basani** - [muralibasani](https://github.com/muralibasani)

## License

This project is licensed under the Apache License  - see the [LICENSE.md](LICENSE.md) file for details.

## Architecture:

![Architecture](https://github.com/muralibasani/kafkawize/blob/master/screenshots/arch.png)

## Dependency project:

### ClusterApi
https://github.com/muralibasani/kafkawizeclusterapi

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

- Synchronization from and to kafka clusters
- Reconciliation and email notifications on differences between Kafkawize and Clusters
- Restore configuration (topics, acls)

- Login 
  - Active directory integration
  - Single Sign-on (OAuth2)
  - Basesd on database
  
- Configure Clusters and Environments
  - Clusters can be created connecting to Kafka clusters. (Cluster Management Api should be configured)
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

- Kafka Connectivity
  - PLAINTEXT, SSL, SASL

- Audit
  - All topic, acl, schema and connector requests

- Email notifications when
  - requests are created, approved, declined
  - users are created, approved

- Help Wizard to setup Kafkawize

- Documentation : https://kafkawize.readthedocs.io/en/latest

## Install

mvn clean install

and follow steps defined at https://kafkawize.readthedocs.io/en/latest

[![Setup steps](https://yt-embed.herokuapp.com/embed?v=LOqjwARmbBs)](https://www.youtube.com/playlist?list=PLP9lIqI_cSE72D-VxYFEHW8T7VgS1NWmP "Kafkawize setup")

## Support

Glad to discuss, if you are looking for professional support.

## Colloboration

If you have some free time and like to learn and be part of this widely used project, this is an opportunity. Colloborate to the project. We are more than happy to onboard you.
