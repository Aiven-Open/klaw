# Kafkawize

Kafkawize is a Self service Apache Kafka Topic Management tool/portal. It is a web application which automates the process of creating and browsing Kafka topics, acls, schemas by introducing roles/authorizations to users of various teams of an organization.

## Getting Started

To get Kafkwize running on your local machine or server, please follow the instructions mentioned in [GettingStarted - ReadTheDocs](https://kafkawize.readthedocs.io/en/latest/getting_started.html)

### Prerequisites

```
Intellij/Eclipse/Netbeans
Kafkawize metastore (Rdbms or Cassandra)
Free licence key
others..
```

Detailed in page [Prerequisites](https://kafkawize.readthedocs.io/en/latest/getting_started.html#prerequisites)

### Installing

```
Install Cassandra/Rdbms
Install license key
Update configuration and mvn install kafkawize and kafkawizeclusterapi
Start Kafka,Zookeeper
Access Kafkawize
```

A step by step guide is explained in this page [DetailedInstallation](https://kafkawize.readthedocs.io/en/latest/getting_started.html)

## Running the tests

Unit and Integration tests are available in [kafkawize](https://github.com/muralibasani/kafkawize) and [kafkawizeclusterapi](https://github.com/muralibasani/kafkawizeclusterapi)

Run a clean install with mvn clean install


## Deployment

```
Install Cassandra/Rdbms
Install license key
Update configuration and mvn install kafkawize and kafkawizeclusterapi
Start Kafka,Zookeeper
Access Kafkawize
Make sure the applications are running without any network/firewall issues
```

## Built With

* Bootstrap UI, Angular, Javascript, HTML, CSS - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* Java, Spring boot, Spring security, SQL, CSQL, Kafka Admin client - The backend development used

## Versioning

For the versions available, see the [tags on this repository](https://github.com/muralibasani/kafkawize/tags). 

## Authors

* **Muralidhar Basani** - [muralibasani](https://github.com/muralibasani)

## License

This project is licensed under the Apache License 2.0  - see the [LICENSE.md](LICENSE.md) file for details

## Watch the Introduction:

[![Watch the Introduction](https://github.com/muralibasani/kafkawize/blob/master/screenshots/arch.png)](https://youtu.be/KOjdpRtRhEY)


