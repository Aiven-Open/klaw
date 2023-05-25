import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";

interface EnvironmentFieldProps {
  environments: ExtendedEnvironment[];
  selectedTopic?: string;
  prefixed?: boolean;
  readOnly?: boolean;
}

const getOptions = (
  environments: ExtendedEnvironment[],
  prefixed: boolean,
  topicName?: string
) => {
  return environments.map((env) => {
    if (prefixed) {
      return (
        <Option key={env.id} value={env.id}>
          {env.name}
        </Option>
      );
    }

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
  prefixed = false,
  readOnly = false,
}: EnvironmentFieldProps) => {
  return (
    <NativeSelect
      name="environment"
      labelText="Environment"
      placeholder={"-- Please select --"}
      required
      readOnly={readOnly}
    >
      {getOptions(environments, prefixed, selectedTopic)}
    </NativeSelect>
  );
};

export default EnvironmentField;
