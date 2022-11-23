import {
  Flexbox,
  Icon,
  InputBase,
  PrimaryButton,
} from "@aivenio/design-system";
import { FormEvent, useState } from "react";
import searchItem from "@aivenio/design-system/dist/module/icons/search";

type SearchTopicsProps = {
  searchTerm: string;
  search: (searchTerm: string) => void;
};

function SearchTopics(props: SearchTopicsProps) {
  const { search, searchTerm } = props;
  const [currentSearchTerm, setCurrentSearchTerm] =
    useState<string>(searchTerm);

  function hasInput() {
    return currentSearchTerm.trim().length > 0;
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!hasInput()) {
      return;
    }
    const trimmed = currentSearchTerm.trim();
    setCurrentSearchTerm(trimmed);
    search(trimmed);
  }

  return (
    <form role={"search"} onSubmit={onSubmit} aria-label={"Topics"}>
      <label className={"visually-hidden"} htmlFor={"topics-search"}>
        Search topics
      </label>
      <Flexbox>
        <InputBase
          placeholder="Topic name"
          value={currentSearchTerm}
          onChange={(event) => setCurrentSearchTerm(event.target.value)}
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
