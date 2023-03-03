import { NativeSelect, Option } from "@aivenio/aquarium";
import {
  ALL_ENVIRONMENTS_VALUE,
  Environment,
  getSchemaRegistryEnvironments,
} from "src/domain/environment";
import { useQuery } from "@tanstack/react-query";

type SelectSchemaRegEnvironmentProps = {
  value: Environment["id"];
  onChange: (value: Environment["id"]) => void;
};

function SelectSchemaRegEnvironment(props: SelectSchemaRegEnvironmentProps) {
  const { value, onChange } = props;

  const { data: environments, isLoading } = useQuery<Environment[], Error>({
    queryKey: ["schemaRegistryEnvironments"],
    queryFn: () => getSchemaRegistryEnvironments(),
  });

  if (isLoading || !environments) {
    return (
      <div data-testid={"select-environment-loading"}>
        <NativeSelect.Skeleton />
      </div>
    );
  } else {
    return (
      <NativeSelect
        labelText="Filter by Environment"
        value={value}
        onChange={(event) => {
          const env = event.target.value as Environment["id"];
          onChange(env);
        }}
      >
        <Option key={ALL_ENVIRONMENTS_VALUE} value={ALL_ENVIRONMENTS_VALUE}>
          All Environments
        </Option>

        {environments.map((env: Environment) => (
          <Option key={env.id} value={env.id}>
            {env.name}
          </Option>
        ))}
      </NativeSelect>
    );
  }
}

export { SelectSchemaRegEnvironment };
