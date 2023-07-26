import { getMetaEnv } from "src/services/get-meta-env";

/** We're enabling users switching between
 * Coral (react app) and the old Klaw UI (Klaw)
 * by routing. For Coral's build, we're using "/coral"
 * as VITE_ROUTER_BASENAME. Corals build file are located
 * in a directory /coral/ in Klaw's assets. That way
 * the navigation can happen smoothly.
 * During development, the VITE_ROUTER_BASENAME for Coral is "/"
 * so when we want to add links that start should start
 * at the root "/", the link has to start with "coral/"
 * for it to work with Angular. This helper function
 * enables us to build a href string based on the BASE_URL
 * that is set by vite in build / development server.
 */
function buildUrl(hrefString: string) {
  const metaEnv = getMetaEnv();
  if (metaEnv === undefined) {
    console.error(`metaEnv should not be empty, please check vite meta data.`);
  }

  const baseUrl = metaEnv?.VITE_ROUTER_BASENAME;

  // 1. check for undefined because:
  // getMetaEnv says it always returns a string, but if
  // the VITE_ROUTER_BASENAME is not set, it's actually undefined
  // 2. "replace":
  // urls are given with a starting "/" and we want to
  // enforce that pattern here too. So we're removing the ending /
  // in base url to make the full href work.
  const base = baseUrl !== undefined ? baseUrl.replace(/\/$/, "") : "";

  return `${base}${hrefString}`;
}

export { buildUrl };
