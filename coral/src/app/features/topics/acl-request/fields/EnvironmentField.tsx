import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";
import { Environment } from "src/domain/environment";

interface EnvironmentFieldProps {
  environments: Environment[];
}

const EnvironmentField = ({ environments }: EnvironmentFieldProps) => {
  return (
    <NativeSelect name="environment" labelText="Select environment" required>
      <Option key={"Placeholder"} value="placeholder" disabled>
        -- Select Environment --
      </Option>
      {environments.map((env) => (
        <Option key={env.id} value={env.id}>
          {env.name}
        </Option>
      ))}
    </NativeSelect>
  );
};

export default EnvironmentField;
