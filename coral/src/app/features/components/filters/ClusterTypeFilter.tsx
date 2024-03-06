import { NativeSelect } from "@aivenio/aquarium";
import { ChangeEvent } from "react";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { ClusterType } from "src/domain/cluster";
import { clusterTypeMapList } from "src/services/formatter/cluster-type-formatter";

function ClusterTypeFilter() {
  const { clusterType, setFilterValue } = useFiltersContext();

  const handleChangeClusterType = (e: ChangeEvent<HTMLSelectElement>) => {
    const nextOperationType = e.target.value as ClusterType;

    setFilterValue({ name: "clusterType", value: nextOperationType });
  };

  return (
    <NativeSelect
      labelText={"Filter by cluster type"}
      key={"filter-cluster-type"}
      defaultValue={clusterType}
      onChange={handleChangeClusterType}
    >
      {clusterTypeMapList.map((clusterType) => {
        return (
          <option key={clusterType.value} value={clusterType.value}>
            {clusterType.value === "ALL"
              ? "All cluster types"
              : clusterType.name}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { ClusterTypeFilter };
