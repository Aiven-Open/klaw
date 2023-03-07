import { SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useSearchParams } from "react-router-dom";

function TopicFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const topic = searchParams.get("topic") ?? undefined;

  const handleChangeTopic = (nextTopic: string) => {
    if (nextTopic === "") {
      searchParams.delete("topic");
      searchParams.set("page", "1");
      setSearchParams(searchParams);
    } else {
      searchParams.set("topic", nextTopic);
      searchParams.set("page", "1");
      setSearchParams(searchParams);
    }
  };

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
          return handleChangeTopic(parsedTopic);
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
