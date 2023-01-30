import isString from "lodash/isString";
import { objectHasProperty } from "src/services/type-utils";

function parseErrorMsg(error: unknown): string {
  if (
    objectHasProperty(error, "data") &&
    objectHasProperty(error.data, "message")
  ) {
    if (isString(error.data.message)) {
      return error.data.message;
    }
  }
  return "Unexpected error";
}

export { parseErrorMsg };
