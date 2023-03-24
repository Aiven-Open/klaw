import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import {
  Environment,
  getEnvironments,
  getSchemaRegistryEnvironments,
} from "src/domain/environment";
import { HTTPError } from "src/services/api";

interface EnvironmentFilterProps {
  isSchemaRegistryEnvironments?: boolean;
}

function EnvironmentFilter({
  isSchemaRegistryEnvironments = false,
}: EnvironmentFilterProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  const environment = searchParams.get("environment") ?? "ALL";

  const { data: environments } = useQuery<Environment[], HTTPError>(
    ["topic-environments"],
    {
      queryFn: isSchemaRegistryEnvironments
        ? getSchemaRegistryEnvironments
        : getEnvironments,
    }
  );

  function handleChangeEnv(newEnvironment: string) {
    const isAllEnvironments = newEnvironment === "ALL";
    if (isAllEnvironments) {
      searchParams.delete("environment");
      searchParams.set("page", "1");
    } else {
      searchParams.set("environment", newEnvironment);
      searchParams.set("page", "1");
    }
    setSearchParams(searchParams);
  }

  if (!environments) {
    return (
      <div data-testid={"select-environment-loading"}>
        <NativeSelect.Skeleton />
      </div>
    );
  } else {
    return (
      <NativeSelect
        labelText="Filter by Environment"
        value={environment}
        onChange={(event) => handleChangeEnv(event.target.value)}
      >
        <Option key={"ALL"} value={"ALL"}>
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

export default EnvironmentFilter;
