import {
  act,
  cleanup,
  fireEvent,
  render,
  screen,
} from "@testing-library/react";
import { useContext } from "react";
import {
  createMemoryRouter,
  Link,
  Outlet,
  RouterProvider,
  useLocation,
} from "react-router-dom";
import RequestModal from "src/app/layout/request-modal/RequestModal";
import { RequestModalContext } from "src/app/hooks/useRequestFormClose";
import { Routes as AppRoutes } from "src/services/router-utils/types";

function LocationDisplay() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}</div>;
}

// Simulates a request form: Cancel uses the modal context, and the form
// reports its dirty state via setRequestDirty
function FormWithCancelButton({ dirty = false }: { dirty?: boolean }) {
  const context = useContext(RequestModalContext);
  if (dirty) context?.setRequestDirty?.(true);
  return (
    <div>
      Request topic form
      <button onClick={context?.requestClose}>Cancel</button>
    </div>
  );
}

function renderAt(
  initialEntries: string[],
  initialIndex?: number,
  formDirty = false
) {
  const router = createMemoryRouter(
    [
      {
        path: "/",
        element: (
          <>
            <LocationDisplay />
            <Outlet />
          </>
        ),
        children: [
          {
            path: AppRoutes.TOPICS,
            element: (
              <div>
                Topics page
                <Link to={AppRoutes.TOPIC_REQUEST}>Open request modal</Link>
              </div>
            ),
          },
          {
            path: AppRoutes.TOPIC_REQUEST,
            element: (
              <RequestModal
                title="Request topic"
                confirmTitle="Cancel topic request?"
              >
                <FormWithCancelButton dirty={formDirty} />
              </RequestModal>
            ),
          },
          {
            path: AppRoutes.TOPIC_SCHEMA_REQUEST,
            element: (
              <RequestModal
                title={({ topicName }: { topicName?: string }) =>
                  `Request new schema for topic "${topicName}"`
                }
                confirmTitle="Cancel schema request?"
              >
                <FormWithCancelButton />
              </RequestModal>
            ),
          },
        ],
      },
    ],
    { initialEntries, initialIndex }
  );
  return { router, ...render(<RouterProvider router={router} />) };
}

describe("RequestModal", () => {
  afterEach(() => {
    cleanup();
  });

  it("closes the modal back to the origin page via the close button", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1);

    expect(screen.getByRole("dialog")).toBeVisible();
    expect(screen.getByText("Request topic")).toBeVisible();
    expect(screen.getByText("Request topic form")).toBeVisible();

    fireEvent.click(screen.getByRole("button", { name: "Close" }));

    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("closes the modal with Escape and returns to the origin page", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1);

    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });

    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);
  });

  it("falls back to the dashboard on direct load (no history)", () => {
    const { router } = renderAt([AppRoutes.TOPIC_REQUEST]);

    fireEvent.click(screen.getByRole("button", { name: "Close" }));

    expect(screen.getByTestId("location").textContent).toBe(
      AppRoutes.DASHBOARD
    );
    // replaced, so browser back does not reopen the closed form
    expect(router.state.historyAction).toBe("REPLACE");
  });

  it("form cancel button closes the modal back to the origin page", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1);

    fireEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("shows the cancel confirmation instead of closing when the form is dirty", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1, true);

    fireEvent.click(screen.getByRole("button", { name: "Close" }));

    expect(
      screen.getByRole("alertdialog", { name: "Cancel topic request?" })
    ).toBeVisible();
    expect(screen.getByTestId("location").textContent).toBe(
      AppRoutes.TOPIC_REQUEST
    );
  });

  it("closes the modal when the user confirms the cancel in the dirty form", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1, true);

    fireEvent.click(screen.getByRole("button", { name: "Close" }));
    fireEvent.click(screen.getByRole("button", { name: "Cancel request" }));

    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);
  });

  it("dismisses the confirmation and stays on the form when the user continues", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1, true);

    fireEvent.click(screen.getByRole("button", { name: "Close" }));
    fireEvent.click(
      screen.getByRole("button", { name: "Continue with request" })
    );

    expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
    expect(screen.getByTestId("location").textContent).toBe(
      AppRoutes.TOPIC_REQUEST
    );
  });

  it("does not show the confirmation again when the modal is reopened after a confirmed cancel", () => {
    renderAt([AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST], 1, true);

    // dirty form, confirm the cancel -> back on topics page
    fireEvent.click(screen.getByRole("button", { name: "Close" }));
    fireEvent.click(screen.getByRole("button", { name: "Cancel request" }));
    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);

    // reopen the modal: no leftover confirmation dialog
    fireEvent.click(screen.getByRole("link", { name: "Open request modal" }));
    expect(screen.getByRole("dialog")).toBeVisible();
    expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
  });

  it("does not show the confirmation in the next modal when the user left with browser back while it was open", () => {
    const { router } = renderAt(
      [AppRoutes.TOPICS, AppRoutes.TOPIC_REQUEST],
      1,
      true
    );

    // dirty form, the confirmation is open
    fireEvent.click(screen.getByRole("button", { name: "Close" }));
    expect(screen.getByRole("alertdialog")).toBeVisible();

    // browser back leaves the modal route without using closeModal
    act(() => {
      router.navigate(-1);
    });
    expect(screen.getByTestId("location").textContent).toBe(AppRoutes.TOPICS);

    // reopen the modal: no leftover confirmation dialog
    fireEvent.click(screen.getByRole("link", { name: "Open request modal" }));
    expect(screen.getByRole("dialog")).toBeVisible();
    expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
  });

  it("uses the route params for a title given as function", () => {
    renderAt([AppRoutes.TOPICS, "/topic/my-topic/request-schema"], 1);

    expect(
      screen.getByText('Request new schema for topic "my-topic"')
    ).toBeVisible();
  });
});
