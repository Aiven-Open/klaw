import { Box } from "@aivenio/aquarium";
import { TextInput } from "src/app/components/Form";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { CreateAclRequestTopicTypeProducer } from "src/domain/acl";

interface TopicNameOrPrefixFieldProps {
  topicNames: string[];
  aclPatternType?: CreateAclRequestTopicTypeProducer["aclPatternType"];
}

const TopicNameOrPrefixField = ({
  topicNames,
  aclPatternType,
}: TopicNameOrPrefixFieldProps) => {
  if (aclPatternType === "LITERAL") {
    return <TopicNameField topicNames={topicNames} />;
  }

  if (aclPatternType === "PREFIXED") {
    return <TextInput name="topicname" labelText="Prefix" required />;
  }

  // Return empty element matching the height of other inputs to prevent layout shift
  return <Box data-testid="empty" style={{ height: "87px" }} />;
};

export default TopicNameOrPrefixField;
