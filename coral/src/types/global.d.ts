import { MswInstance } from "src/services/api-mocks/types";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import * as React from "@types/react";

// @types/react for v18 removed the implicit children from React.FC.
// The following type override is to enable component types from DS,
// which still use React 17 type declaration and rely on implicit children.
declare module "react" {
  // eslint-disable-next-line @typescript-eslint/ban-types
  interface FunctionComponent<P = {}> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (props: PropsWithChildren<P>, context?: any): ReactElement<any, any> | null;
  }
}

declare global {
  interface Window {
    msw: MswInstance;
  }
}

export {};
