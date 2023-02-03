import { getNotificationCounts } from "src/domain/notification/notification-api";
import api from "src/services/api";
import { server } from "src/services/api-mocks/server";
import { mockGetNotificationCounts } from "src/domain/notification/notification-api.msw";

describe("notification-api", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("getNotificationCounts", () => {
    beforeEach(() => {
      mockGetNotificationCounts({
        mswInstance: server,
        response: {
          data: {
            notifications: "6",
            notificationsAcls: "5",
            notificationsConnectors: "2",
            notificationsUsers: "1",
            notificationsSchemas: "4",
          },
        },
      });
    });
    it("calls the correct API endpoint", async () => {
      const getSpy = jest.spyOn(api, "get");
      const result = await getNotificationCounts();
      expect(getSpy).toHaveBeenCalledTimes(1);
      expect(getSpy).toHaveBeenLastCalledWith("/getAuth");
      expect(result).toEqual({
        topicNotificationCount: 6,
        aclNotificationCount: 5,
        connectorNotificationCount: 2,
        userNotificationCount: 1,
        schemaNotificationCount: 4,
      });
    });
  });
});
