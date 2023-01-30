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

export { objectHasProperty };
