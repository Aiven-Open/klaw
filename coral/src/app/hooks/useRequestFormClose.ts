import { createContext, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * Context provided by RequestModal to the request form rendered inside it.
 * It is `null` on regular pages (no request modal).
 */
type RequestModalContextValue = {
  // Closes the request modal. When the form is dirty, shows the cancel
  // confirmation first instead of closing.
  requestClose: () => void;
  setRequestDirty: (dirty: boolean) => void;
};

const RequestModalContext = createContext<RequestModalContextValue | null>(
  null
);

// For request forms rendered inside RequestModal reports the form's dirty state
// to the modal and returns the close handler
function useRequestFormClose(form: { formState: { isDirty: boolean } }) {
  const context = useContext(RequestModalContext);
  const navigate = useNavigate();
  const isDirty = form.formState.isDirty;

  useEffect(() => {
    context?.setRequestDirty(isDirty);
  });

  return context?.requestClose ?? (() => navigate(-1));
}

export { useRequestFormClose, RequestModalContext };
export type { RequestModalContextValue };
