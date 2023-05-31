import { TopicMessages } from "src/domain/topic/topic-types";
import { TopicMessageItem } from "src/app/features/topics/details/messages/components/TopicMessageItem";

type Props = {
  messages: TopicMessages;
};

function TopicMessageList({ messages }: Props) {
  return (
    <>
      {Object.keys(messages).map((offsetId) => (
        <TopicMessageItem
          key={offsetId}
          offsetId={offsetId}
          message={messages[offsetId] ?? ""}
        />
      ))}
    </>
  );
}

export { TopicMessageList };
