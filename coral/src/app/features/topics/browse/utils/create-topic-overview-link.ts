const urlPartKlawAngularApp = "topicOverview?topicname=";

function createTopicOverviewLink(topicName: string): string {
  const originLocationKlawAngular = window.location.origin;
  return `${originLocationKlawAngular}/${urlPartKlawAngularApp}${topicName}`;
}

export { createTopicOverviewLink };
