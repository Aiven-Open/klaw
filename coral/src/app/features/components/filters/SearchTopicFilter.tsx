import { SearchFilter } from "src/app/features/components/filters/SearchFilter";

function SearchTopicFilter() {
  return (
    <SearchFilter
      label={"Search Topic"}
      placeholder={"my-topic-billings"}
      description={`Partial match for topic name.`}
      ariaDescription={`Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
    />
  );
}

export { SearchTopicFilter };
