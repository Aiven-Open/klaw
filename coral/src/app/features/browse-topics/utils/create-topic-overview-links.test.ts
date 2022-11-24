import { createTopicOverviewLink } from "src/app/features/browse-topics/utils/create-topic-overview-link";

describe("create-topic-overview-links", () => {
  beforeAll(() => {
    // deleting the global window.location allows us to set it to a URL for easier testing
    // otherwise the assignment is ignored
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    delete window.location;
  });

  it("creates a link based on a topic name and the origin location", () => {
    const testWindowLocationOrigin = "http://klaw.com";
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    window.location = new URL(testWindowLocationOrigin);

    const link = createTopicOverviewLink("testTopic");

    expect(link).toEqual("http://klaw.com/topicOverview?topicname=testTopic");
  });

  it("creates a link based on a topic name  and the origin location with port", () => {
    const testWindowLocationOrigin = "http://localhost:8080";
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    window.location = new URL(testWindowLocationOrigin);

    const link = createTopicOverviewLink("testTopic");
    expect(link).toEqual(
      "http://localhost:8080/topicOverview?topicname=testTopic"
    );
  });
});
