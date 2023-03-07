import { NativeSelect, Option } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import { useGetEnvironments } from "src/app/features/topics/browse/hooks/environment/useGetEnvironments";
import { Environment } from "src/domain/environment";

function EnvironmentFilter() {
  const [searchParams, setSearchParams] = useSearchParams();

  const environment = searchParams.get("environment") ?? "ALL";

  const { data: environments } = useGetEnvironments();

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
