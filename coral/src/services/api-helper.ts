import cloneDeepWith from "lodash/cloneDeepWith";
import isPlainObject from "lodash/isPlainObject";
import toString from "lodash/toString";

type QueryToTransform = {
  [key: string]: string | boolean | number | QueryToTransform;
};

function convertQuery(query: QueryToTransform): Record<string, string> {
  return cloneDeepWith(query, (value) => {
    return isPlainObject(value) ? undefined : toString(value);
  });
}

export { convertQuery };
