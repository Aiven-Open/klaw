import { TextInput } from "src/app/components/Form";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { CreateAclRequestTopicTypeProducer } from "src/domain/acl";

interface TopicNameOrPrefixFieldProps {
  topicNames: string[];
  aclPatternType: CreateAclRequestTopicTypeProducer["aclPatternType"];
}

const TopicNameOrPrefixField = ({
  topicNames,
  aclPatternType,
}: TopicNameOrPrefixFieldProps) => {
  if (aclPatternType === "LITERAL") {
    return <TopicNameField topicNames={topicNames} />;
  }

  return <TextInput name="topicname" labelText="Prefix" required />;
};

export default TopicNameOrPrefixField;
