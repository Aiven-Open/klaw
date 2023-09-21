import { Box, Button, IconButton, Typography } from "@aivenio/aquarium";
import cross from "@aivenio/aquarium/icons/cross";
import { ReactElement, useEffect } from "react";
import { createPortal } from "react-dom";
import classes from "src/app/components/Modal.module.css";

type ModalAction = {
  text: string;
  onClick: () => void;
  loading?: boolean;
  disabled?: boolean;
};

type ModalProps = {
  title: string;
  subtitle?: string;
  close?: () => void;
  primaryAction: ModalAction;
  secondaryAction?: ModalAction;
  children: ReactElement;
  isDialog?: boolean;
  dialogTitle?: ReactElement;
};

function Modal(props: ModalProps) {
  const {
    close,
    primaryAction,
    secondaryAction,
    children,
    title,
    subtitle,
    isDialog = false,
    dialogTitle,
  } = props;

  function setFocus(appRoot: HTMLElement, modal: HTMLElement) {
    appRoot.setAttribute("aria-hidden", "true");
    appRoot.setAttribute("tabindex", "-1");
    appRoot.setAttribute("inert", "true");
    modal.setAttribute("tabindex", "0");
    modal.focus();
  }

  function removeFocus(appRoot: HTMLElement, modal: HTMLElement) {
    appRoot.removeAttribute("aria-hidden");
    appRoot.removeAttribute("tabindex");
    appRoot.removeAttribute("inert");
    modal.removeAttribute("tabindex");
  }

  useEffect(() => {
    const appRoot = document.getElementById("root");
    const modalFocus = document.getElementById("modal-focus");
    if (appRoot && modalFocus) {
      setFocus(appRoot, modalFocus);
      return () => {
        removeFocus(appRoot, modalFocus);
      };
    }
  }, []);

  useEffect(() => {
    if (close) {
      const handleEscape = (event: KeyboardEvent) => {
        if (event.key !== "Escape") return;
        close();
      };

      window.addEventListener("keydown", handleEscape);

      return () => {
        window.removeEventListener("keydown", handleEscape);
      };
    }
  }, []);

  useEffect(() => {
    const isFirefox = window.navigator.userAgent.includes("Firefox");

    if (isFirefox) {
      const focusableElements =
        document.querySelectorAll<HTMLElement>("[data-focusable]");
      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];

      const handleTab = (event: KeyboardEvent) => {
        if (event.key !== "Tab") return;

        if (!event.shiftKey && document.activeElement === lastElement) {
          event.preventDefault();
          firstElement.focus();
        }

        if (event.shiftKey && document.activeElement === firstElement) {
          event.preventDefault();
          lastElement.focus();
        }
      };

      window.addEventListener("keydown", handleTab);

      return () => {
        window.removeEventListener("keydown", handleTab);
      };
    }
  }, []);

  return (
    <>
      {createPortal(
        <Box.Flex
          display={"flex"}
          alignItems={"center"}
          justifyContent={"center"}
          className={classes.modalWrapper}
        >
          <Box.Flex
            component={"dialog"}
            aria-modal={"true"}
            paddingX={"l3"}
            paddingY={"l2"}
            display={"flex"}
            flexDirection={"column"}
            borderRadius={"4px"}
            backgroundColor={"white"}
            width={"6/12"}
            // value is arbitrary, it should prevent buttons overflowing
            // the modal in a very small screen
            //eslint-disable-next-line @typescript-eslint/ban-ts-comment
            //@ts-ignore*
            minWidth={"600px"}
          >
            <Box.Flex
              display={"flex"}
              justifyContent={"space-between"}
              alignItems={"start"}
              paddingBottom={"l2"}
            >
              <div>
                {!isDialog && (
                  <>
                    <Typography.Subheading
                      htmlTag={"h1"}
                      id={"modal-focus"}
                      data-focusable
                    >
                      {title}
                    </Typography.Subheading>
                    {subtitle && (
                      <Typography.SmallText htmlTag={"h2"} color={"grey-60"}>
                        {subtitle}
                      </Typography.SmallText>
                    )}
                  </>
                )}
                {isDialog && dialogTitle && <>{dialogTitle}</>}
              </div>
              {close && (
                <IconButton
                  aria-label="Close modal"
                  icon={cross}
                  onClick={close}
                  data-focusable
                  disabled={secondaryAction?.loading || primaryAction.loading}
                />
              )}
            </Box.Flex>
            <Box.Flex className={classes.modalTextBlock}>{children}</Box.Flex>
            <Box.Flex
              paddingTop={"l2"}
              display={"flex"}
              justifyContent={"end"}
              colGap={"l1"}
            >
              {secondaryAction && (
                <Button
                  kind={isDialog ? "ghost" : "secondary"}
                  onClick={secondaryAction.onClick}
                  data-focusable
                  disabled={secondaryAction.disabled}
                  loading={secondaryAction.loading}
                  className={"test"}
                >
                  {secondaryAction.text}
                </Button>
              )}
              <Button
                kind={isDialog ? "secondary" : "primary"}
                onClick={primaryAction.onClick}
                data-focusable
                disabled={primaryAction.disabled}
                loading={primaryAction.loading}
              >
                {primaryAction.text}
              </Button>
            </Box.Flex>
          </Box.Flex>
        </Box.Flex>,
        document.body
      )}
    </>
  );
}

export { Modal };
export type { ModalProps };
