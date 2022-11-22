function getRouterBasename(): string | undefined {
  return process.env.ROUTER_BASENAME;
}

function getHTTPBaseAPIUrl(): string | undefined {
  return process.env.API_BASE_URL ?? location.origin;
}

export { getRouterBasename, getHTTPBaseAPIUrl };
