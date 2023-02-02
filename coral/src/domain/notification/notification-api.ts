import { KlawApiResponse } from "types/utils";
import api from "src/services/api";
import { transformGetNotificationCounts } from "src/domain/notification/notification-transformer";
import { Notifications } from "src/domain/notification/notification-types";

function getNotificationCounts(): Promise<Notifications> {
  // Using the GET /getAuth endpoint is not the most obvious API to
  // fetch notification counts. However, this endpoint is used for the
  // current Angular application as well, so lets use it in Coral as well
  // before we get special purpose API endpoints.
  return api
    .get<KlawApiResponse<"getAuth">>("/getAuth")
    .then(transformGetNotificationCounts);
}

export { getNotificationCounts };
