import { SearchFilter } from "src/app/features/components/filters/SearchFilter";

function SearchConnectorFilter() {
  return (
    <SearchFilter
      key="search"
      label="Search Connector"
      placeholder={"local-file-source"}
      description={"A partial match for connector name."}
      ariaDescription={`Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
    />
  );
}

export { SearchConnectorFilter };
