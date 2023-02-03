import { transformGetNotificationCounts } from "src/domain/notification/notification-transformer";

describe("notification-transformer", () => {
  describe("transformGetNotificationCounts", () => {
    it("transforms the api payload correctly", () => {
      const res = transformGetNotificationCounts({
        notifications: "1",
        notificationsAcls: "2",
        notificationsSchemas: "3",
        notificationsConnectors: "4",
        notificationsUsers: "5",
      });
      expect(res).toEqual({
        topicNotificationCount: 1,
        aclNotificationCount: 2,
        schemaNotificationCount: 3,
        connectorNotificationCount: 4,
        userNotificationCount: 5,
      });
    });
  });
});
