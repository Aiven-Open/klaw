import { SearchFilter } from "src/app/features/components/filters/SearchFilter";

function SearchConnectorFilter() {
  return (
    <SearchFilter
      key="search"
      placeholder={"Search Connector name"}
      description={`Search for a partial match for connector name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
    />
  );
}

export { SearchConnectorFilter };
