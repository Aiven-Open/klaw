import { getClusterInfo } from "src/domain/environment/environment-api";

const clusterInfoFromEnvironment = ({
  envSelected,
  envType,
}: {
  envSelected: string;
  envType: string;
}) => {
  return {
    queryKey: ["clusterInfoFromEnvironment", envSelected, envType],
    queryFn: () => getClusterInfo({ envSelected, envType }),
    keepPreviousData: true,
  };
};

export { clusterInfoFromEnvironment };
