import isString from "lodash/isString";
import { objectHasProperty } from "src/services/type-utils";

function parseErrorMsg(error: unknown): string {
  // Error and KlawApiError will look like this:
  // { message: "error message",  [properties] }
  // where message is always a string
  // in case the message is an empty string,
  // we want to fallback on our default message
  if (
    objectHasProperty(error, "message") &&
    isString(error.message) &&
    error.message.length > 0
  ) {
    return error.message;
  }

  // e.g. see structure of error we return in
  // handleError as HTTPError
  // same as above, we want to default to
  // the default message in case the string
  // is empty
  if (
    objectHasProperty(error, "data") &&
    objectHasProperty(error.data, "message") &&
    isString(error.data.message) &&
    error.data.message.length > 0
  ) {
    return error.data.message;
  }

  return "Unexpected error";
}

export { parseErrorMsg };
