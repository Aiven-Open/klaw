import { KlawApiResponse } from "types/utils";
import { Notifications } from "src/domain/notification/notification-types";

function transformGetNotificationCounts(
  apiResponse: KlawApiResponse<"getAuth">
): Notifications {
  return {
    topicNotificationCount: parseStringIntoNumber(apiResponse.notifications),
    aclNotificationCount: parseStringIntoNumber(apiResponse.notificationsAcls),
    connectorNotificationCount: parseStringIntoNumber(
      apiResponse.notificationsConnectors
    ),
    schemaNotificationCount: parseStringIntoNumber(
      apiResponse.notificationsSchemas
    ),
    userNotificationCount: parseStringIntoNumber(
      apiResponse.notificationsUsers
    ),
  };
}

function parseStringIntoNumber(value: string): number {
  return parseInt(value, 10);
}

export { transformGetNotificationCounts };
