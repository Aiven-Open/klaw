import { NativeSelect, Option } from "@aivenio/aquarium";
import { useEffect, useState } from "react";
import { ALL_ENVIRONMENTS_VALUE, Environment } from "src/domain/environment";
import { useSearchParams } from "react-router-dom";
import { useGetEnvironments } from "src/app/features/topics/browse/hooks/environment/useGetEnvironments";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";

type SelectEnvProps = {
  onChange: (value: string) => void;
};

function SelectEnv(props: SelectEnvProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  const initialEnvironment = searchParams.get("environment");
  const [environment, setEnvironment] = useState<string>(
    ENVIRONMENT_NOT_INITIALIZED
  );
  const { onChange } = props;

  const { data: environments } = useGetEnvironments();

  useEffect(() => {
    if (initialEnvironment) {
      setEnvironment(initialEnvironment);
    }
    // updates `environment` in BrowseTopics
    // which will trigger the api call
    onChange(initialEnvironment || ALL_ENVIRONMENTS_VALUE);
  }, [initialEnvironment]);

  function onChangeEnv(newEnvironment: string) {
    const isAllTeams = newEnvironment === ALL_ENVIRONMENTS_VALUE;
    if (isAllTeams) {
      searchParams.delete("environment");
    } else {
      searchParams.set("environment", newEnvironment);
    }
    setEnvironment(newEnvironment);
    onChange(newEnvironment);
    setSearchParams(searchParams);
  }

  if (!environments || !environment) {
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
        onChange={(event) => onChangeEnv(event.target.value)}
      >
        <Option key={ALL_ENVIRONMENTS_VALUE} value={ALL_ENVIRONMENTS_VALUE}>
          All environments
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

export default SelectEnv;
