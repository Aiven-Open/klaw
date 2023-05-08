import { SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import type { ChangeEvent } from "react";

function SearchFilter() {
  const { search, setFilterValue } = useFiltersValues();
  return (
    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={`Search Connector name`}
        defaultValue={search.toString()}
        onChange={debounce(
          (event: ChangeEvent<HTMLInputElement>) =>
            setFilterValue({
              name: "search",
              value: String(event.target.value).trim(),
            }),
          500
        )}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Search for an partial match in name. Searching starts automatically with
        a little delay while typing. Press &quot;Escape&quot; to delete all your
        input.
      </div>
    </div>
  );
}

export default SearchFilter;
