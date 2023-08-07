/* eslint-disable @typescript-eslint/no-var-requires */

// eslint-disable-next-line no-undef
const http = require("http");
// eslint-disable-next-line no-undef
const httpProxy = require("http-proxy");
// eslint-disable-next-line no-undef
const HttpProxyRules = require("http-proxy-rules");
const {
  proxyPort,
  proxyConfigurations,
  verboseMode,
  // eslint-disable-next-line no-undef
} = require("./config");
const {
  printRulesAsTable,
  isPrintableRequest,
  // eslint-disable-next-line no-undef
} = require("./rules");
const {
  getAllRules,
  buildProxyRulesConfig,
  // eslint-disable-next-line no-undef
} = require("./rules");

const defaultLocalhostAddress = "http://localhost";
const ruleConfigs = buildProxyRulesConfig(
  proxyConfigurations,
  defaultLocalhostAddress
);

const proxy = httpProxy.createProxyServer();
const proxyRules = new HttpProxyRules({ rules: ruleConfigs });

function proxyRequest(req, res) {
  const originalUrl = req.url;
  const target = proxyRules.match(req);
  const isApiRequest = originalUrl.startsWith("/api/");

  if (target) {
    const options = {
      target: isApiRequest ? target.replace("/api/", "/") : target,
      secure: false,
      changeOrigin: true,
    };

    if (verboseMode || isPrintableRequest(originalUrl)) {
      console.log(
        `\nMethod:\t ${req.method}\nRequest:\t ${originalUrl}\nTarget:\t ${target}\n`
      );
    }
    return proxy.web(req, res, options, (error) =>
      console.error("Failed to proxy: ", error)
    );
  }

  const errorMessage = `😢 Could not match a rule for: ${originalUrl}. Please check config.js\n`;
  console.error(errorMessage);

  res.writeHead(500, { "Content-Type": "text/plain" });
  res.end(errorMessage);
}

function createAndStartHttpServer() {
  const httpServer = http.createServer(proxyRequest);

  // listen to the `upgrade` event and proxy the web socket requests
  // to enable hot module replacement from vite
  httpServer.on("upgrade", function (req, socket, head) {
    proxy.ws(req, socket, head, {
      target: proxyRules.match(req),
      secure: false,
    });
  });

  httpServer.listen(proxyPort);

  // extend timeouts to avoid disconnecting from services which are temporarily not available
  // see: https://connectreport.com/blog/tuning-http-keep-alive-in-node-js/

  // default header timeout 60 seconds https://nodejs.org/api/http.html#http_server_headerstimeout
  const defaultHeadersTimeout = httpServer.headersTimeout;
  const additionalToleranceSecond = 1000;

  httpServer.keepAliveTimeout =
    defaultHeadersTimeout + additionalToleranceSecond;

  // has to be longer than `keepAliveTimeout`!
  httpServer.headersTimeout =
    defaultHeadersTimeout + 2 * additionalToleranceSecond;
}

function printRules() {
  const sortedRules = getAllRules(ruleConfigs, defaultLocalhostAddress);
  printRulesAsTable(sortedRules);

  console.info(
    `\n🎉 Klaw is running in the proxy server now.\n\nℹ️  Please go to http://localhost:${proxyPort}\n`
  );
}

createAndStartHttpServer();
printRules();
