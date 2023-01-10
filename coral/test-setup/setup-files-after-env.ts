import "@testing-library/jest-dom";
import "whatwg-fetch";

process.env.API_BASE_URL = "http://localhost:8080";
process.env.FEATURE_FLAG_TOPIC_REQUEST = "true";