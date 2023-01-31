import { KlawApiResponse } from "types/utils";
import api from "src/services/api";
import { transformGetNotificationCounts } from "src/domain/notification/notification-transformer";
import { Notifications } from "src/domain/notification/notification-types";

function getNotificationCounts(): Promise<Notifications> {
  return api
    .get<KlawApiResponse<"getAuth">>("/getAuth")
    .then(transformGetNotificationCounts);
}

export { getNotificationCounts };
