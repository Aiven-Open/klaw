/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_ROUTER_BASENAME: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
