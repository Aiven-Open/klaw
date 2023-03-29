import isString from "lodash/isString";
import { objectHasProperty } from "src/services/type-utils";

function parseErrorMsg(error: unknown): string {
  if (objectHasProperty(error, "data")) {
    if (
      objectHasProperty(error.data, "message") &&
      isString(error.data.message)
    ) {
      return error.data.message;
    }
    if (
      objectHasProperty(error.data, "result") &&
      isString(error.data.result)
    ) {
      return error.data.result;
    }
  }
  return "Unexpected error";
}

export { parseErrorMsg };
