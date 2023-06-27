const topicDetailsAngularKlawRoute = "topicOverview?topicname=";
const connectorDetailsAngularKlawRoute = "connectorOverview?connectorName=";

function createTopicOverviewLink(topicName: string): string {
  const originLocationKlawAngular = window.location.origin;
  return `${originLocationKlawAngular}/${topicDetailsAngularKlawRoute}${topicName}`;
}

function createConnectorOverviewLink(connectorName: string): string {
  const originLocationKlawAngular = window.location.origin;
  return `${originLocationKlawAngular}/${connectorDetailsAngularKlawRoute}${connectorName}`;
}

export { createTopicOverviewLink, createConnectorOverviewLink };
