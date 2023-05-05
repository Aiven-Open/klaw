import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";

interface EnvironmentFieldProps {
  environments: ExtendedEnvironment[];
}

const EnvironmentField = ({ environments }: EnvironmentFieldProps) => {
  return (
    <NativeSelect
      name="environment"
      labelText="Environment"
      placeholder={"-- Please select --"}
      required
    >
      {environments.map((env) => {
        if (env.topicNames.length === 0) {
          return (
            <Option key={env.id} value={env.id} disabled>
              {env.name} (no topic available)
            </Option>
          );
        }
        return (
          <Option key={env.id} value={env.id}>
            {env.name}
          </Option>
        );
      })}
    </NativeSelect>
  );
};

export default EnvironmentField;
