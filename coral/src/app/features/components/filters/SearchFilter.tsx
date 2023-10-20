import { Box, Label, SearchInput, Typography } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import type { ChangeEvent } from "react";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";

type SearchFilterProps = {
  label: string;
  placeholder: string;
  description: string;
  ariaDescription: string;
};

function SearchFilter({
  label,
  placeholder,
  description,
  ariaDescription,
}: SearchFilterProps) {
  const { search, setFilterValue } = useFiltersContext();
  return (
    <Box maxWidth={"md"}>
      <Label id={"search-field-label"}>
        <Typography.SmallStrong color={"grey-60"}>
          {label}
        </Typography.SmallStrong>
      </Label>

      <SearchInput
        type={"search"}
        aria-label={placeholder}
        aria-labelledby={"search-field-label"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={placeholder}
        aria-description={ariaDescription}
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

      <Box marginTop={"1"} marginBottom={"3"}>
        <Typography.Caption id={"search-field-description"}>
          {description}
          <div className={"visually-hidden"}>{ariaDescription}</div>
        </Typography.Caption>
      </Box>
    </Box>
  );
}

export { SearchFilter };
