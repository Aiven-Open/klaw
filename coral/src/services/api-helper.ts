import cloneDeepWith from "lodash/cloneDeepWith";
import isPlainObject from "lodash/isPlainObject";
import toString from "lodash/toString";

type QueryToTransform = {
  [key: string]: string | boolean | number | string[] | QueryToTransform;
};

function convertQueryValuesToString(
  query: QueryToTransform
): Record<string, string> {
  return cloneDeepWith(query, (value) => {
    if (Array.isArray(value)) {
      return value.join(",");
    }
    // returning undefined if the value for the customizer function
    // is an object makes sure we're iterating over everything
    // this is not strictly needed for our current use-case
    // but added to be more versatile usable in the future
    return isPlainObject(value) ? undefined : toString(value);
  });
}

export { convertQueryValuesToString };
