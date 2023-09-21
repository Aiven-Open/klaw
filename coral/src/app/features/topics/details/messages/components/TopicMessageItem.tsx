import { BorderBox, Box, Button, Typography } from "@aivenio/aquarium";
import expandIcon from "@aivenio/aquarium/icons/chevronRight";
import minimizeIcon from "@aivenio/aquarium/icons/chevronDown";
import { useState, useId } from "react";
import truncate from "lodash/truncate";

type Props = {
  offsetId: string;
  message: string;
};

function TopicMessageItem({ offsetId, message }: Props) {
  const [expanded, setExpanded] = useState<boolean>(false);
  const panelTrigger = useId();
  const panelId = useId();

  function getMessage(): React.ReactNode {
    if (!message) {
      return (
        <Typography.SmallStrong color="grey-40">
          <i>Empty message</i>
        </Typography.SmallStrong>
      );
    } else if (expanded) {
      return <Typography.SmallStrong>{message}</Typography.SmallStrong>;
    } else {
      return (
        <Typography.SmallStrong>
          {truncate(message, { length: 100 })}
        </Typography.SmallStrong>
      );
    }
  }

  return (
    <BorderBox padding={"4"} marginBottom={"4"}>
      <Box.Flex component={"h3"}>
        <Box.Flex marginRight="2">
          <Button.Icon
            id={panelTrigger}
            icon={expanded ? minimizeIcon : expandIcon}
            aria-label={`Expand message ${offsetId}`}
            disabled={!message}
            aria-expanded={expanded}
            aria-controls={panelId}
            onClick={() => setExpanded(!expanded)}
          />
        </Box.Flex>
        <Box.Flex
          paddingTop="2"
          grow={1}
          id={panelId}
          aria-labelledby={panelTrigger}
        >
          {getMessage()}
        </Box.Flex>
      </Box.Flex>
    </BorderBox>
  );
}

export { TopicMessageItem };
