import { Box, Label, SearchInput, Typography } from "@aivenio/aquarium";
import debounce from "lodash/debounce";
import uniqueId from "lodash/uniqueId";
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
  const descriptionId = uniqueId("search-field-description");
  const labelId = uniqueId("search-field-label");

  return (
    <Box maxWidth={"md"}>
      <Label id={labelId}>
        <Typography.SmallStrong color={"grey-60"}>
          {label}
        </Typography.SmallStrong>
      </Label>

      <SearchInput
        aria-label={placeholder}
        aria-labelledby={labelId}
        aria-describedby={descriptionId}
        placeholder={placeholder}
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
        <Typography.Caption id={descriptionId}>
          {description}
          <div className={"visually-hidden"}>{ariaDescription}</div>
        </Typography.Caption>
      </Box>
    </Box>
  );
}

export { SearchFilter };
