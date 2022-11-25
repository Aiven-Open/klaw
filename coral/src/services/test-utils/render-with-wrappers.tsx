import { QueryClientProvider } from "@tanstack/react-query";
import { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";

function withQueryClient(ui: ReactElement, options?: RenderOptions) {
  const queryClient = getQueryClientForTests();
  render(ui, {
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
  render(ui, {
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
  render(ui, {
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

type CustomRenderOption = {
  queryClient?: boolean;
  memoryRouter?: boolean;
  customRoutePath?: string;
};
function customRender(
  ui: ReactElement,
  renderWith?: CustomRenderOption,
  options?: RenderOptions
) {
  if (!renderWith) {
    return render(ui, options);
  }
  if (renderWith.queryClient && renderWith.memoryRouter) {
    return withMemoryRouterAndQueryClient({
      ui,
      customRoutePath: renderWith.customRoutePath,
      options,
    });
  }

  if (renderWith.memoryRouter) {
    return withMemoryRouter({
      ui,
      customRoutePath: renderWith.customRoutePath,
      options,
    });
  }
  if (renderWith.queryClient) {
    return withQueryClient(ui, options);
  }
}
export { customRender };
