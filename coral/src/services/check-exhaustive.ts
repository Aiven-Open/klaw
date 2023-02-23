const checkExhaustive = (param: never): never => {
  throw new Error("Missing or unexpected param", param);
};

export { checkExhaustive };
