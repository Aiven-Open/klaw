import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";

interface EnvironmentFieldProps {
  environments: ExtendedEnvironment[];
  selectedTopic?: string;
}

const getOptions = (
  environments: ExtendedEnvironment[],
  topicName?: string
) => {
  return environments.map((env) => {
    if (env.topicNames.length === 0) {
      return (
        <Option key={env.id} value={env.id} disabled>
          {env.name} (no topic available)
        </Option>
      );
    }

    if (topicName !== undefined && !env.topicNames.includes(topicName)) {
      return (
        <Option key={env.id} value={env.id} disabled>
          {env.name} (unavailable for selected topic)
        </Option>
      );
    }

    return (
      <Option key={env.id} value={env.id}>
        {env.name}
      </Option>
    );
  });
};

const EnvironmentField = ({
  environments,
  selectedTopic,
}: EnvironmentFieldProps) => {
  return (
    <NativeSelect
      name="environment"
      labelText="Environment"
      placeholder={"-- Please select --"}
      required
    >
      {getOptions(environments, selectedTopic)}
    </NativeSelect>
  );
};

export default EnvironmentField;
