import { WindowMswWorkerInstance } from "src/domain/api-mocks/types";

declare let window: WindowMswWorkerInstance;

function getWindowWithMswInstance(): WindowMswWorkerInstance {
  return window;
}

export { getWindowWithMswInstance };
