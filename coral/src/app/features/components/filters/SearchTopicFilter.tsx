import { SearchFilter } from "src/app/features/components/filters/SearchFilter";

function SearchTopicFilter() {
  return (
    <SearchFilter
      placeholder={"Search Topic name"}
      description={`Search for a partial match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
    />
  );
}

export { SearchTopicFilter };
