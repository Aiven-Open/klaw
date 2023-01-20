import { defineConfig, loadEnv, PluginOption, ProxyOptions } from "vite";
import react from "@vitejs/plugin-react";
import { visualizer } from "rollup-plugin-visualizer";
import { resolve } from "path";
import fs from "fs";

/**
 * Get basename for React router.
 *
 * @param  {[Record<string, string>]} environment
 *
 * Picks first encountered value:
 * 1. $VITE_ROUTER_BASENAME
 * 2. $BASE_URL
 * 3. undefined (default)
 */
function getRouterBasename(env: Record<string, string>): string | undefined {
  return env.VITE_ROUTER_BASENAME ?? env.BASE_URL ?? undefined;
}

/**
 * Get base url for Klaw API client.
 *
 * @param  {[type]} environment
 *
 * The $VITE_API_BASE_URL variable allows API to be consumed from another origin.
 * Also, this make it easy to override the API base url for unittests.
 */
function getApiBaseUrl(env: Record<string, string>): string | undefined {
  return env.VITE_API_BASE_URL ?? undefined;
}

/**
 * Get development server HTTPS config.
 *
 * @param  {[type]} environment
 *
 * run development server in HTTP mode if if $VITE_SERVER_CERTIFICATE_PATH
 * and $VITE_SERVER_CERTIFICATE_KEY_PATH are defined. This is needed when
 * using a remote backend that is running under HTTPS.
 */
function getServerHTTPSConfig(
  env: Record<string, string>
): false | { key: Buffer; cert: Buffer } {
  if (
    env.VITE_SERVER_CERTIFICATE_PATH &&
    env.VITE_SERVER_CERTIFICATE_KEY_PATH
  ) {
    return {
      key: fs.readFileSync(env.VITE_SERVER_CERTIFICATE_KEY_PATH),
      cert: fs.readFileSync(env.VITE_SERVER_CERTIFICATE_PATH),
    };
  }
  return false;
}

/**
 * Get development server Klaw API proxy target.
 *
 * @param  {[type]} environment
 *
 * Use $VITE_PROXY_TARGET or Klaw API development default (http://localhost:9097)
 */
function getProxyTarget(env: Record<string, string>): string {
  const origin = env.VITE_PROXY_TARGET ?? "http://localhost:9097";
  return `${new URL(origin).origin}`;
}

/**
 * Get development server Klaw API proxy target.
 *
 * @param  {[type]} environment
 *
 * Use $VITE_PROXY_TARGET or Klaw API development default (http://localhost:9097)
 */
function getServerProxyConfig(
  env: Record<string, string>
): Record<string, string | ProxyOptions> | undefined {
  const LEGACY_LOGIN_RESOURCES = [
    "/login",
    "/lib/angular.min.js",
    "/lib/angular-route.min.js",
    "/js/loginSaas.js",
    "/assets/css/",
    "/assets/js/",
    "/assets/plugins/",
    "/assets/images/",
  ];
  const target = getProxyTarget(env);
  const secure = false;
  return {
    "/api": {
      target,
      rewrite: (path) => path.replace(/^\/api/, ""),
      secure,
    },
    ...LEGACY_LOGIN_RESOURCES.reduce(
      (acc, current) => ({
        ...acc,
        [current]: {
          target,
          secure,
        },
      }),
      {}
    ),
  };
}

function getPlugins(environment: Record<string, string>): PluginOption[] {
  const plugins: PluginOption[] = [react()];
  if (environment.BUNDLE_ANALYZE) {
    plugins.push(visualizer());
  }
  return plugins;
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const environment = loadEnv(mode, process.cwd(), "");
  return {
    plugins: getPlugins(environment),
    define: {
      // Vite does not use process.env (see https://vitejs.dev/guide/env-and-mode.html).
      // If a library depends on process.env (like "@aivenio/aquarium").
      // ⛔ Note: there are stackoverflow answers / github issues that recommend e.g
      // ⛔ 'process.env': process.env or
      // ⛔ 'process.env': { ...process.env}
      // ⛔️ Don't do that! This can expose unwanted env vars in production builds.
      "process.env": {
        ROUTER_BASENAME: getRouterBasename(environment),
        API_BASE_URL: getApiBaseUrl(environment),
        FEATURE_FLAG_TOPIC_REQUEST: ["development", "remote-api"]
          .includes(mode)
          .toString(),
      },
    },
    css: {
      modules: {
        localsConvention: "camelCaseOnly",
      },
    },
    resolve: {
      alias: {
        src: resolve(resolve(__dirname), "./src"),
      },
    },
    server: {
      port: 5173,
      https: getServerHTTPSConfig(environment),
      proxy: getServerProxyConfig(environment),
    },
    preview: {
      port: 5173,
      https: getServerHTTPSConfig(environment),
      proxy: getServerProxyConfig(environment),
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: (id: string) => {
            if (id.includes("node_modules")) {
              return "vendor";
            }
          },
        },
      },
    },
  };
});
