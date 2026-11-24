import { Box, Modal } from "@aivenio/aquarium";
import { ReactNode, useRef, useState } from "react";
import { Params, useLocation, useNavigate, useParams } from "react-router-dom";
import { CancelRequestDialog } from "src/app/features/components/CancelRequestDialog";
import {
  RequestModalContext,
  RequestModalContextValue,
} from "src/app/hooks/useRequestFormClose";
import { Routes } from "src/services/router-utils/types";

type RequestModalProps = {
  title: string | ((params: Params) => string);
  confirmTitle: string;
  children: ReactNode;
};

// The modal around a request form. Request routes are wrapped in it by
// requestModalRoute (see router.tsx).
function RequestModal({ title, confirmTitle, children }: RequestModalProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const params = useParams();
  const [confirmVisible, setConfirmVisible] = useState(false);
  const dirtyRef = useRef(false);

  function closeModal() {
    // `location.key` is "default" on a direct page load (e.g. direct link)
    // there is no history entry to go back to. Falls back to DASHBOARD
    // in that case.
    if (location.key === "default") {
      navigate(Routes.DASHBOARD, { replace: true });
    } else {
      navigate(-1);
    }
  }

  function requestClose() {
    if (dirtyRef.current) {
      setConfirmVisible(true);
    } else {
      closeModal();
    }
  }

  const context: RequestModalContextValue = {
    requestClose,
    setRequestDirty: (dirty) => {
      dirtyRef.current = dirty;
    },
  };

  return (
    <RequestModalContext.Provider value={context}>
      <Modal
        open
        size="full"
        title={typeof title === "function" ? title(params) : title}
        onClose={requestClose}
      >
        <Box
          style={{ margin: "0 auto" }}
          width={{ default: "full", md: "4/5" }}
        >
          {children}
        </Box>
      </Modal>
      <CancelRequestDialog
        open={confirmVisible}
        title={confirmTitle}
        onConfirm={closeModal}
        onDismiss={() => setConfirmVisible(false)}
      />
    </RequestModalContext.Provider>
  );
}

export default RequestModal;
