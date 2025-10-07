import "@testing-library/jest-dom";
import "whatwg-fetch";
import * as process from "process";
import "src/services/test-utils/mock-documenation-helper";
import failOnConsole from "jest-fail-on-console";

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

failOnConsole({
  shouldFailOnWarn: true,
  shouldFailOnError: true,
  silenceMessage(message) {
    // remove console.warn from tests until update react router 7
    return /React Router Future Flag Warning/.test(message);
  },
});
