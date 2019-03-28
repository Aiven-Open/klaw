
Download the full bundle from https://kafkawize.com/download-kafkawize/ and follow the instructions.

<b>Kafkawize</b> : A Web application which automates the process of creating and browsing Kafka topics, acls, schemas, by introducing  roles/authorizations to users of various teams of an organization

<h4>About the Application</h4> 

<h5>Objective :</h5>
Automate the process of creating and browsing Kafka components, by introducing roles, teams and users.<br><br>

<h5>Technical Architecture:</h5>

![ImageFig1](https://github.com/kafkawize/kafkawize/blob/master/screenshots/arch.png)

Kafkawize contains two main APIs. (User Interface API  and Cluster management API)

User Interface API directly communicates between users and Cluster API.<br><br>
Front end is built with AngularJs, HTML, and Java script.<br>

Cluster API acts as middle layer between Kafka brokers and UserInterface API.<br>
Cluster API creates Kafka Admin Client and executes the requests for Topic, Acls or Schema registry.<br>

Apache Cassandra Or RDBMS(MySql for ex) datastore stores all users, teams, topicRequests, request and execution data from all the users., and to maintain source of truth.<br>

Spring Security, Spring Boot frameworks, Hibernate are used to develop this application.<br>

<h5>Functionalities:</h5><br> (Broadly divided into two categories based on user roles.)<br>
ROLE : USER  can request for creation of kafka components, and browse kafka components.<br>
ROLE : ADMIN  can approve and execute the requests of users to create kafka components.<br>
ROLE : SUPERUSER can synchronize topicRequests meta information with Cassandra/Rdbms Datastore from Kafka Brokers (Source of Truth.)<br>

<b>Browse:</b> (ROLE : USER, ADMIN, SUPERUSER)<br>
All users can Browse Topics<br>
All users can Browse Acls<br>
All users can view the producers and consumers of all topicRequests. <br>

<b>Requests:</b>(ROLE : USER)<br>
Users can request for Kafka Topics <br>
Users can request for Kafka Acl <br>
Users can request for Schemas to be registered on Confluent Schema registry. <br>
Users can view all the requests from his team. <br>

<b>Clusters:</b>(ROLE : USER)<br>
Users can view the available environments <br>

<b>Approve - Execute :</b>(ROLE : ADMIN)<br>
Users can appprove requests for creating Kafka Topics <br>
Users can appprove requests for creating Kafka Acls <br>
Users can appprove requests for uploading schemas on topicRequests<br>

<b>Users :</b>(ROLE : ADMIN)<br>
Users can view all user details <br>
Users can add new users <br>

<b>Teams :</b>(ROLE : ADMIN, SUPERUSER)<br>
Users can view all team details <br>
Users can add new team <br>

<b>Clusters:</b>(ROLE : SUPERUSER)<br>
Users can add a new environment environments  <br>

<b>Synchronize Metadata :</b>(ROLE : SUPERUSER)<br>
Users can synchronize topicRequest information from Brokers with Cassandra/Rdbms datastore. (Update team info.) <br>
Users can synchronize acls information from Brokers with Cassandra/Rdbms datastore. (Update team info.) <br>

<b>My Profile :</b>(ROLE : USER, ADMIN)<br>
Users can view their profile. <br>

<b>Change Password :</b>(ROLE : USER, ADMIN)<br>
Users can change their passwords. <br>

<b>Logout :</b>(ROLE : USER, ADMIN)<br>
Users can logout. <br>

<b>How to Run the application</b>

Download the full bundle from https://kafkawize.com/download-kafkawize/ and follow the instructions.


<b>Screenshots</b>

![ImageFig10](https://github.com/kafkawize/kafkawize/blob/master/screenshots/login.JPG)

![ImageFig8](https://github.com/kafkawize/kafkawize/blob/master/screenshots/BrowseTopics.JPG)

![ImageFig7](https://github.com/kafkawize/kafkawize/blob/master/screenshots/BrowseAcls.JPG)

![ImageFig12](https://github.com/kafkawize/kafkawize/blob/master/screenshots/ProducersConsumers.JPG)

![ImageFig14](https://github.com/kafkawize/kafkawize/blob/master/screenshots/RequestTopic.JPG)

![ImageFig13](https://github.com/kafkawize/kafkawize/blob/master/screenshots/RequestACL.JPG)

![ImageFig11](https://github.com/kafkawize/kafkawize/blob/master/screenshots/MyRequests.JPG)

![ImageFig6](https://github.com/kafkawize/kafkawize/blob/master/screenshots/ApproveTopics.JPG)

![ImageFig5](https://github.com/kafkawize/kafkawize/blob/master/screenshots/ApproveACL.JPG)

![ImageFig15](https://github.com/kafkawize/kafkawize/blob/master/screenshots/SynchronizeAcls.JPG)

![ImageFig16](https://github.com/kafkawize/kafkawize/blob/master/screenshots/SynchronizeTopics.JPG)

![ImageFig2](https://github.com/kafkawize/kafkawize/blob/master/screenshots/AddCluster.JPG)

![ImageFig9](https://github.com/kafkawize/kafkawize/blob/master/screenshots/Environments.JPG)

![ImageFig3](https://github.com/kafkawize/kafkawize/blob/master/screenshots/AddTeam.JPG)

![ImageFig4](https://github.com/kafkawize/kafkawize/blob/master/screenshots/AddUser.JPG)

![ImageFig17](https://github.com/kafkawize/kafkawize/blob/master/screenshots/ActivityLog.JPG)
