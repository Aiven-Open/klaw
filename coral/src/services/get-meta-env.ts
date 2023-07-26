// import.meta.env does crash
// jest. Returning it in one specific
// util function makes it convenient
// to mock it in test when needed.
function getMetaEnv() {
  return import.meta.env;
}

export { getMetaEnv };
