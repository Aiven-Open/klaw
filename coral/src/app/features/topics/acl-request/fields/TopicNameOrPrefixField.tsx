import { Input } from "@aivenio/aquarium";
import { TextInput } from "src/app/components/Form";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { CreateAclRequestTopicTypeProducer } from "src/domain/acl";

interface TopicNameOrPrefixFieldProps {
  topicNames: string[];
  aclPatternType?: CreateAclRequestTopicTypeProducer["aclPatternType"];
  readOnly?: boolean;
}

const TopicNameOrPrefixField = ({
  topicNames,
  aclPatternType,
  readOnly = false,
}: TopicNameOrPrefixFieldProps) => {
  if (aclPatternType === "LITERAL") {
    return <TopicNameField topicNames={topicNames} readOnly={readOnly} />;
  }

  if (aclPatternType === "PREFIXED") {
    return <TextInput name="topicname" labelText="Prefix" required />;
  }

  return (
    // This is not really a readOnly field but
    // a placeholder until the user can select value
    // from a list, so I didn't change the label
    <Input
      labelText="Topic name or prefix"
      defaultValue="Select environment and topic pattern type first"
      height={45}
      readOnly
      required={true}
    />
  );
};

export default TopicNameOrPrefixField;
