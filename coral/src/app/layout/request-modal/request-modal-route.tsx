import { PathParam, RouteObject } from "react-router-dom";
import RequestModal from "src/app/layout/request-modal/RequestModal";
import { Routes } from "src/services/router-utils/types";

/** The params of a route path, e.g. `{ topicName: string }` for `/topic/:topicName` */
type RouteParams<Path extends string> = { [key in PathParam<Path>]: string };

type RequestModalRoute<Path extends Routes> = {
  path: Path;
  element: RouteObject["element"];
  /** Modal title. A function gets the params of this route, e.g. to show a topic name. */
  title: string | ((params: RouteParams<Path>) => string);
  confirmTitle: string;
};

/**
 * Route for a request form, shown to the user as a full-screen modal on top
 * of the page it was opened from. The element is wrapped in
 * a RequestModal, so Cancel/close/ESC navigates back to the origin page or a fallback.
 *
 * @param path - Route path. Also determines the params type passed to a
 *   `title` function.
 * @param element - The request form component.
 * @param title - Modal title shown in the header. Pass a function to include
 *   route params, typed from `path`: for `/topic/:topicName` the function
 *   receives `{ topicName: string }`.
 * @param confirmTitle - Title of the "unsaved changes" dialog shown when the
 *   user tries to close a dirty form.
 */
function requestModalRoute<Path extends Routes>({
  path,
  element,
  title,
  confirmTitle,
}: RequestModalRoute<Path>): RouteObject {
  return {
    path,
    element: (
      <RequestModal
        // RequestModal calls the title with the params of the matched route,
        // which always contain every `:param` of `path`. The generic `Params`
        // type of the modal can't express that, so this is the one place
        // where the params get their specific type.
        title={
          typeof title === "function"
            ? (params) => title(params as RouteParams<Path>)
            : title
        }
        confirmTitle={confirmTitle}
      >
        {element}
      </RequestModal>
    ),
  };
}

export { requestModalRoute };
