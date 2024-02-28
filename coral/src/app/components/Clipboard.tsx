import { Tooltip, PositionerPlacement, Button } from "@aivenio/aquarium";
import duplicate from "@aivenio/aquarium/icons/duplicate";
import { useRef, useState, useEffect, FormEvent } from "react";

const copyToClipboard = async (text: string): Promise<void> => {
  try {
    await navigator.clipboard.writeText(text);
  } catch (error) {
    console.error("Failed to copy to clipboard:", error);
  }
};

const ClipBoard = ({
  text,
  accessibleCopyDescription,
  accessibleCopiedDescription,
  description,
}: {
  text: string;
  accessibleCopyDescription: string;
  accessibleCopiedDescription: string;
  description?: string;
}) => {
  const feedbackTimerRef = useRef<number>();

  const [showCopyFeedback, setShowCopyFeedback] = useState(false);

  const clearTimeouts = () => {
    window.clearTimeout(feedbackTimerRef.current);
  };

  useEffect(() => () => clearTimeouts(), []);

  function handleCopy(event: FormEvent) {
    event.preventDefault();
    copyToClipboard(text);
    setShowCopyFeedback(true);
    feedbackTimerRef.current = window.setTimeout(
      () => setShowCopyFeedback(false),
      1000
    );
  }

  if (showCopyFeedback) {
    return (
      <Tooltip
        key="copied"
        content="Copied"
        isOpen
        placement={PositionerPlacement.top}
      >
        <Button.SecondaryGhost
          key="copy-button"
          aria-label={accessibleCopiedDescription}
          onClick={handleCopy}
          icon={duplicate}
        >
          {description}
        </Button.SecondaryGhost>
        <div aria-live="polite" className={"visually-hidden"}>
          {accessibleCopiedDescription}
        </div>
      </Tooltip>
    );
  }

  return (
    <Tooltip
      key="copy-to-clipboard"
      content="Copy"
      placement={PositionerPlacement.top}
    >
      <Button.SecondaryGhost
        key="copy-button"
        aria-label={accessibleCopyDescription}
        onClick={handleCopy}
        icon={duplicate}
      >
        {description}
      </Button.SecondaryGhost>
    </Tooltip>
  );
};

export default ClipBoard;
