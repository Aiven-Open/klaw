import { WindowMswWorkerInstance } from "src/services/api-mocks/types";

declare let window: WindowMswWorkerInstance;

function getWindowWithMswInstance(): WindowMswWorkerInstance {
  return window;
}

export { getWindowWithMswInstance };
