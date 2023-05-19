import { SearchInput } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import type { ChangeEvent } from "react";

type SearchFilterProps = {
  placeholder: string;
  description: string;
  paginated?: boolean;
};

function SearchFilter({
  placeholder,
  description,
  paginated,
}: SearchFilterProps) {
  const { search, setFilterValue } = useFiltersValues();
  return (
    <>
      <SearchInput
        type={"search"}
        aria-label={placeholder}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={placeholder}
        defaultValue={search.toString()}
        onChange={debounce(
          (event: ChangeEvent<HTMLInputElement>) =>
            setFilterValue({
              name: "search",
              value: String(event.target.value).trim(),
              paginated,
            }),
          500
        )}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        {description}
      </div>
    </>
  );
}

export { SearchFilter };
