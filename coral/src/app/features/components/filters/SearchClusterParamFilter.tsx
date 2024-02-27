import { SearchFilter } from "src/app/features/components/filters/SearchFilter";

function SearchClusterParamFilter() {
  return (
    <SearchFilter
      label={"Search Cluster parameters"}
      placeholder={"kafkaconnect"}
      description={
        "Partial match for: Cluster name, bootstrap server and protocol."
      }
      ariaDescription={`Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
    />
  );
}

export { SearchClusterParamFilter };
