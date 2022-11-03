import { MswInstance } from "src/services/api-mocks/types";

export {};

declare global {
  interface Window {
    msw: MswInstance;
  }
}
