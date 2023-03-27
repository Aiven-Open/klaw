import { SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";

function TopicFilter() {
  const { topic, setFilterValue } = useFiltersValues();

  return (
    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={"Search Topic"}
        defaultValue={topic}
        onChange={debounce((e) => {
          const parsedTopic = String(e.target.value).trim();
          return setFilterValue({ name: "topic", value: parsedTopic });
        }, 500)}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Search for an exact match for topic name. Searching starts automatically
        with a little delay while typing. Press &quot;Escape&quot; to delete all
        your input.
      </div>
    </div>
  );
}

export default TopicFilter;
