import { MswInstance } from "src/services/api-mocks/types";

declare global {
  interface Window {
    msw: MswInstance;
  }
}

export {};
