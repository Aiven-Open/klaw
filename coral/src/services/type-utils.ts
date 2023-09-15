function objectHasProperty<T extends string>(
  object: unknown,
  key: T
): object is Record<T, unknown> {
  return (
    object !== null &&
    object !== undefined &&
    Object.prototype.hasOwnProperty.call(object, key)
  );
}

export { objectHasProperty };
