function getAllRules(ruleConfigs) {
  return Object.entries(ruleConfigs).map((ruleConfig) => {
    return { request: ruleConfig[0], target: ruleConfig[1] };
  });
}

function convertToProxyRules(config) {
  return Object.fromEntries(
    Object.entries(config.rules).map(([from, to]) => {
      // keeping this as a variable in case we
      // want to use https:// on some ports
      const protocol = "http://";
      const target = `${protocol}localhost:${config.localPort}${to}`;

      return [from, target];
    })
  );
}

function mergeRules(previousRules, currentRules) {
  return { ...previousRules, ...currentRules };
}

function buildProxyRulesConfig(proxyConfigurations) {
  return proxyConfigurations
    .map((rule) => {
      return convertToProxyRules(rule);
    })
    .reduce(mergeRules, {});
}

function printRulesAsTable(rules) {
  if (rules.length === 0) {
    console.error(`\n ⚠️ Redirect rules should not be empty.`);
  } else {
    console.log(`\n🚛 Redirected:`);
    rules.forEach((rule) => {
      console.log(`from ${rule.request}  -->  ${rule.target}`);
    });
  }
}

function isPrintableRequest(request) {
  return (
    !request.includes("@fs") &&
    !request.includes("/node_modules/") &&
    !request.includes("/src/") &&
    !request.includes("/@vite/client") &&
    !request.includes("/@react-refresh") &&
    !request.includes("/favicon.png") &&
    !request.includes("/assets/")
  );
}

// eslint-disable-next-line no-undef
module.exports = {
  buildProxyRulesConfig,
  getAllRules,
  printRulesAsTable,
  isPrintableRequest,
};
