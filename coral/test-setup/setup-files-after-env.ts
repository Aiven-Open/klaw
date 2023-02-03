import "@testing-library/jest-dom";
import "whatwg-fetch";
import * as process from "process";
//eslint-disable-next-line no-relative-import-paths/no-relative-import-paths

process.env.API_BASE_URL = "http://localhost:8080";
process.env.FEATURE_FLAG_TOPIC_REQUEST = "true";
