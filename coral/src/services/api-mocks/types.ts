import { SetupWorkerApi } from "msw";
import { SetupServerApi } from "msw/node";

type MswInstance = SetupWorkerApi | SetupServerApi;

export type { MswInstance };
