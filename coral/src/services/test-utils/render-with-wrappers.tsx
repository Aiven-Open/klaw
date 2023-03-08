import { QueryClientProvider } from "@tanstack/react-query";
import { render, RenderOptions } from "@testing-library/react";
import { ReactElement } from "react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";

function withQueryClient(ui: ReactElement, options?: RenderOptions) {
  const queryClient = getQueryClientForTests();
  return render(ui, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
    ...options,
  });
}

function withMemoryRouter({
  ui,
  customRoutePath,
  options,
}: {
  ui: ReactElement;
  customRoutePath?: string;
  options?: RenderOptions;
}) {
  return render(ui, {
    wrapper: ({ children }) => (
      <MemoryRouter initialEntries={[customRoutePath || ""]}>
        {children}
      </MemoryRouter>
    ),
    ...options,
  });
}

function withMemoryRouterAndQueryClient({
  ui,
  customRoutePath,
  options,
}: {
  ui: ReactElement;
  customRoutePath?: string;
  options?: RenderOptions;
}) {
  const queryClient = getQueryClientForTests();
  return render(ui, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[customRoutePath || ""]}>
          {children}
        </MemoryRouter>
      </QueryClientProvider>
    ),
    ...options,
  });
}

// Only use if BrowserRouter is really needed
// prefer MemoryRouter for tests.
function withBrowserRouterAndQueryClient({
  ui,
  options,
}: {
  ui: ReactElement;
  options?: RenderOptions;
}) {
  const queryClient = getQueryClientForTests();
  return render(ui, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>{children}</BrowserRouter>
      </QueryClientProvider>
    ),
    ...options,
  });
}

// To be used for testing URL states (ie, search params)
function withBrowserRouter({
  ui,
  options,
}: {
  ui: ReactElement;
  options?: RenderOptions;
}) {
  return render(ui, {
    wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
    ...options,
  });
}

type ValidOptions =
  | "queryClient"
  | "memoryRouter"
  | "browserRouter"
  | "customRoutePath";

type CustomRenderOption = {
  [K in ValidOptions]?: K extends
    | "queryClient"
    | "memoryRouter"
    | "browserRouter"
    ? boolean
    : string;
};

function customRender(
  ui: ReactElement,
  renderWith: CustomRenderOption,
  options?: RenderOptions
) {
  if (renderWith?.queryClient && renderWith?.memoryRouter) {
    return withMemoryRouterAndQueryClient({
      ui,
      customRoutePath: renderWith?.customRoutePath,
      options,
    });
  }

  if (renderWith?.queryClient && renderWith?.browserRouter) {
    return withBrowserRouterAndQueryClient({ ui, options });
  }

  if (renderWith?.memoryRouter) {
    return withMemoryRouter({
      ui,
      customRoutePath: renderWith?.customRoutePath,
      options,
    });
  }

  if (renderWith?.browserRouter) {
    return withBrowserRouter({
      ui,
      options,
    });
  }

  if (renderWith?.queryClient) {
    return withQueryClient(ui, options);
  }

  console.error(
    "You have not passed any renderWith option, which returns the non-custom render."
  );
  return render(ui, options);
}
export { customRender };
