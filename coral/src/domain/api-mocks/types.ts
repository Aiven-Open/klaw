import { SetupWorkerApi } from "msw";
import { SetupServerApi } from "msw/node";

interface WindowMswWorkerInstance extends Window {
  msw: SetupWorkerApi;
}

type MswInstance = SetupWorkerApi | SetupServerApi;

export type { WindowMswWorkerInstance, MswInstance };
