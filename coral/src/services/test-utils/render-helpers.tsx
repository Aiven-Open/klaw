import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import React, { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { MemoryRouter, MemoryRouterProps } from "react-router-dom";

function withQueryClient({ children }: { children: ReactElement }) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

function withMemoryRouter({
  children,
  options,
}: {
  children: ReactElement;
  options?: MemoryRouterProps;
}) {
  return <MemoryRouter {...options}>{children}</MemoryRouter>;
}

function renderWithWrapper(
  ui: ReactElement,
  wrapper: React.JSXElementConstructor<{ children: React.ReactElement }>,
  options?: RenderOptions
) {
  render(ui, { wrapper, ...options });
}

type RenderWithMemoryRouterProps = {
  renderOptions?: RenderOptions;
  memoryRouterProps?: MemoryRouterProps;
};

function renderWithMemoryRouter(
  ui: ReactElement,
  options?: RenderWithMemoryRouterProps
) {
  renderWithWrapper(ui, withMemoryRouter, options?.renderOptions);
}

function renderWithQueryClient(ui: ReactElement, options?: RenderOptions) {
  renderWithWrapper(ui, withQueryClient, options);
}

function renderWithQueryClientAndMemoryRouter(
  ui: ReactElement,
  options?: RenderWithMemoryRouterProps
) {
  const withQueryClientAndMemoryRouter = ({
    children,
  }: {
    children: ReactElement;
  }) =>
    withMemoryRouter({
      children: withQueryClient({ children }),
      options: options?.memoryRouterProps,
    });
  renderWithWrapper(ui, withQueryClientAndMemoryRouter, options?.renderOptions);
}

export {
  renderWithQueryClient,
  renderWithMemoryRouter,
  renderWithQueryClientAndMemoryRouter,
};
