import { Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import getPaginatedEnvironments from "src/app/features/configuration/environments/hooks/getPaginatedEnvironments";
import {
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
  Routes,
  isEnvironmentsTabEnum,
} from "src/app/router_utils";

type Props = {
  currentTab: EnvironmentsTabEnum;
};

function getTabAriaLabel(
  title: string,
  amountOfEnvs: number | undefined
): string {
  if (typeof amountOfEnvs === "number") {
    if (amountOfEnvs === 0) {
      return `${title}, no environments`;
    } else if (amountOfEnvs === 1) {
      return `${title}, ${amountOfEnvs} environment`;
    } else {
      return `${title}, ${amountOfEnvs} environments`;
    }
  }
  return title;
}

function getBadgeValue(amountOfEnvs: number | undefined): number | undefined {
  if (typeof amountOfEnvs === "number" && amountOfEnvs > 0) {
    return amountOfEnvs;
  }
  return undefined;
}

function EnvironmentsTabs({ currentTab }: Props) {
  const navigate = useNavigate();

  const { environments: kafkaEnvs } = getPaginatedEnvironments({
    type: "kafka",
    currentPage: 1,
  });
  const { environments: schemaRegistryEnvs } = getPaginatedEnvironments({
    type: "schemaregistry",
    currentPage: 1,
  });
  const { environments: kafkaConnectEnvs } = getPaginatedEnvironments({
    type: "kafkaconnect",
    currentPage: 1,
  });
  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title="Kafka"
        value={EnvironmentsTabEnum.KAFKA}
        badge={getBadgeValue(kafkaEnvs?.totalEnvs)}
        aria-label={getTabAriaLabel("Kafka", kafkaEnvs?.totalEnvs)}
      >
        {currentTab === EnvironmentsTabEnum.KAFKA && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Schema Registry"
        value={EnvironmentsTabEnum.SCHEMA_REGISTRY}
        badge={getBadgeValue(schemaRegistryEnvs?.totalEnvs)}
        aria-label={getTabAriaLabel(
          "Schema Registry",
          schemaRegistryEnvs?.totalEnvs
        )}
      >
        {currentTab === EnvironmentsTabEnum.SCHEMA_REGISTRY && <Outlet />}
      </Tabs.Tab>
      <Tabs.Tab
        title="Kafka Connect"
        value={EnvironmentsTabEnum.KAFKA_CONNECT}
        badge={getBadgeValue(kafkaConnectEnvs?.totalEnvs)}
        aria-label={getTabAriaLabel(
          "Kafka Connect",
          kafkaConnectEnvs?.totalEnvs
        )}
      >
        {currentTab === EnvironmentsTabEnum.KAFKA_CONNECT && <Outlet />}
      </Tabs.Tab>
    </Tabs>
  );

  function navigateToTab(
    navigate: NavigateFunction,
    resourceTypeId: unknown
  ): void {
    if (isEnvironmentsTabEnum(resourceTypeId)) {
      navigate(
        `${Routes.ENVIRONMENTS}/${ENVIRONMENT_TAB_ID_INTO_PATH[resourceTypeId]}`
      );
    }
  }
}

export default EnvironmentsTabs;
