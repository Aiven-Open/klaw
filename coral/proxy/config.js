const ports = {
  proxy: 1337,
  klawCore: 9097,
  coral: 5173,
  catchAll: 4444,
};

const proxyConfig = {
  name: "proxy",
  localPort: ports.proxy,
  rules: {},
};

const coralConfig = {
  name: "coral",
  localPort: ports.coral,
  rules: {
    "/coral/(.*)$": "/coral/$1",
  },
};

const klawCoreConfig = {
  name: "klawCore",
  localPort: ports.klawCore,
  rules: {
    "/api/(.*)$": "/api/$1",
    "/login/(.*)$": "/login/$1",
    "/(.*)": "/$1",
  },
};

// eslint-disable-next-line no-undef
module.exports = {
  mainEntryPoint: `http://localhost:${ports.proxy}/coral/`,
  proxyConfigurations: [proxyConfig, coralConfig, klawCoreConfig],
  proxyPort: ports.proxy,
  // eslint-disable-next-line no-undef
  verboseMode: process.env.VERBOSE === "true",
  // with verboseMode true, we will print all requests
  // handled, which can help debugging
};
