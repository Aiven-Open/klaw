import isString from "lodash/isString";
import { objectHasProperty } from "src/services/type-utils";

function parseErrorMsg(error: unknown): string {
  if (
    objectHasProperty(error, "data") &&
    objectHasProperty(error.data, "message") &&
    isString(error.data.message)
  ) {
    return String(error.data.message);
  }
  return "Unexpected error";
}

export { parseErrorMsg };
