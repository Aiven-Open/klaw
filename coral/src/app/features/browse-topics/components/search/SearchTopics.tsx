import {
  Flexbox,
  Icon,
  InputBase,
  PrimaryButton,
} from "@aivenio/design-system";
import { FormEvent, useState } from "react";
import searchItem from "@aivenio/design-system/dist/module/icons/search";

type SearchTopicsProps = {
  value: string;
  onChange: (searchTerm: string) => void;
};

function SearchTopics(props: SearchTopicsProps) {
  const { onChange, value } = props;
  const [searchTerm, setSearchTerm] = useState<string>(value);

  function hasInput() {
    return searchTerm.trim().length > 0;
  }

  function onSearchSubmit(event: FormEvent) {
    event.preventDefault();
    if (!hasInput()) {
      return;
    }
    const trimmed = searchTerm.trim();
    setSearchTerm(trimmed);
    onChange(trimmed);
  }

  return (
    <form role={"search"} onSubmit={onSearchSubmit} aria-label={"Topics"}>
      <label className={"visually-hidden"} htmlFor={"topics-search"}>
        Search topics
      </label>
      <Flexbox>
        <InputBase
          type={"search"}
          placeholder="Topic name"
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
          id={"topics-search"}
          minLength={1}
          required={true}
        />
        <PrimaryButton type={"submit"} dense aria-disabled={!hasInput()}>
          <span className={"visually-hidden"}>Submit search</span>
          <Icon
            aria-hidden={true}
            icon={searchItem}
            data-testid={"visually-hidden-search-icon"}
          />
        </PrimaryButton>
      </Flexbox>
    </form>
  );
}

export { SearchTopics };
