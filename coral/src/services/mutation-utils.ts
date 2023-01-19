import isString from "lodash/isString";

function objectHasProperty<T extends string>(
  object: unknown,
  key: T
): object is Record<T, unknown> {
  if (
    object !== null &&
    object !== undefined &&
    Object.prototype.hasOwnProperty.call(object, key)
  ) {
    return true;
  }
  return false;
}

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
