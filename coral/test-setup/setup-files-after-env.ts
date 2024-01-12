import "@testing-library/jest-dom";
import "whatwg-fetch";
import * as process from "process";
import "src/services/test-utils/mock-documenation-helper";

// Mocking crypto.randomUUID in tests which use it
// ie: components using useToast
Object.defineProperty(global, "crypto", {
  value: {
    randomUUID: () => "",
  },
});

process.env.API_BASE_URL = "http://localhost:8080";
process.env.FEATURE_FLAG_TOPIC_REQUEST = "true";

jest.mock("src/services/is-dev-mode", () => ({
  isDevMode: () => true,
}));
