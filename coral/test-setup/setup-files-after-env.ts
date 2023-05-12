import matchers from "@testing-library/jest-dom/matchers";
import { fetch } from "cross-fetch";
import { expect } from "vitest";
global.fetch = fetch;

// extends Vitest's expect method with methods from react-testing-library
expect.extend(matchers);

//eslint-disable-next-line no-relative-import-paths/no-relative-import-paths

process.env.API_BASE_URL = "http://localhost:8080";
process.env.FEATURE_FLAG_TOPIC_REQUEST = "true";
